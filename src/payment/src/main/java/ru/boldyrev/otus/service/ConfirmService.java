package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.NotSufficientFundsException;
import ru.boldyrev.otus.metrics.ConfirmRejectCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.model.dto.mq.MQOrderItem;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;
import ru.boldyrev.otus.model.entity.PayRequest;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.PaymentResult;
import ru.boldyrev.otus.repo.OrderConfirmRequestRepo;
import ru.boldyrev.otus.repo.PayRequestRepo;


import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfirmService {

    private final OrderConfirmRequestRepo orderRequestRepo;

    private final PayRequestRepo payRequestRepo;

    private final PaymentService paymentService;

    private final ConfirmRejectCounter confirmRejectCounter;


    @Transactional
    public void saveConfirmRequest(OrderConfirmRequest request) {
        orderRequestRepo.saveAndFlush(request);
    }


    public List<OrderConfirmRequest> getOrderRequests(String orderId) {
        return orderRequestRepo.findByOrderId(orderId);
    }


    @Transactional
    public OrderConfirmRequest register(MQOrderConfirmRequest mqRequest) {


        /* Создаем и сохраняем ConfirmRequest */
        OrderConfirmRequest confirmRequest = new OrderConfirmRequest()
                .setOrderId(mqRequest.getTransportableOrder().getId())
                .setConfirmator(mqRequest.getConfirmator())
                .setOrderRequestType(mqRequest.getOrderRequestType())
                .setConfirmStatus(OrderConfirmStatus.IN_PROGRESS)
                .setAmount(calculateAmount(mqRequest.getTransportableOrder()));

        /* Ищем в репозитории одобренный платеж для нашего запроса */
        List<PayRequest> payRequests = payRequestRepo.findByOrderIdAndPaymentResult(/* orderId */mqRequest.getTransportableOrder().getId(), /*PayResult*/ PaymentResult.SUCCESS);

        PayRequest refPayRequest;

        if (confirmRequest.getOrderRequestType() == OrderRequestType.CONFIRM) {


            if (!payRequests.isEmpty()) {
                confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED); /*Одобренный платеж есть*/
                refPayRequest = payRequests.get(0);

                confirmRequest.setPayRequest(refPayRequest);

            } else {

                confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED); /* одобренного платежа нет */
                confirmRejectCounter.increment();
            }
        } else if (confirmRequest.getOrderRequestType() == OrderRequestType.REVERSE) {

            /* Если одобренный платеж есть, его надо отменить */
            if (!payRequests.isEmpty()) {
                try {
                    refPayRequest = paymentService.reverse(payRequests.get(0));
                    confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED);
                    confirmRequest.setPayRequest(refPayRequest);

                } catch (NotSufficientFundsException e) {
                    confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED);
                    confirmRequest.setErrorMessage(e.getMessage());
                    confirmRejectCounter.increment();
                }

            } else { /* Одобренного платежа уже нет */
                confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED);
            }

        }
        confirmRequest = orderRequestRepo.saveAndFlush(confirmRequest);
        return confirmRequest;


    }

    double calculateAmount(MQOrder mqOrder) {
        double amount = 0;
        for (MQOrderItem item : mqOrder.getOrderItems()) {
            amount += item.getQuantity() * item.getProductPrice();
        }
        return amount;
    }
}
