package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.metrics.ConfirmRejectCounter;
import ru.boldyrev.otus.metrics.PickingApplyingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.*;
import ru.boldyrev.otus.model.dto.rest.RestOrderDeliveryCommand;
import ru.boldyrev.otus.model.entity.*;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.repo.CompanyRepo;
import ru.boldyrev.otus.repo.OrderConfirmRequestRepo;
import ru.boldyrev.otus.repo.PickupPointRepo;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfirmService {

    private final OrderConfirmRequestRepo orderConfirmRequestRepo;

    private final NotificationService notificationService;

    private final CompanyRepo companyRepo;

    private final PickupPointRepo pickupPointRepo;

    private final ConfirmRejectCounter confirmRejectCounter;

    private final PickingApplyingErrorCounter pickingApplyingErrorCounter;


    @Transactional
    public OrderConfirmRequest register(MQOrderConfirmRequest mqRequest) {
        OrderConfirmRequest confirmRequest = null;

        if (mqRequest.getOrderRequestType() == OrderRequestType.CONFIRM) {

            /* Поищем уже обработанный confirm */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(mqRequest.getTransportableOrder().getId(), OrderConfirmStatus.CONFIRMED);

            /* Если уже есть, возвращаем его */
            if (!requests.isEmpty())
                return requests.get(0);

        } else if (mqRequest.getOrderRequestType() == OrderRequestType.REVERSE) {

            /* Поищем уже сверсированный confirm */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(mqRequest.getTransportableOrder().getId(), OrderConfirmStatus.REVERSED);

            /* Если уже есть, возвращаем его */
            if (!requests.isEmpty())
                return requests.get(0);
        }

        /* Иначе сохраняем request и возвращаем его */
        confirmRequest = new OrderConfirmRequest()
                .setOrderId(mqRequest.getTransportableOrder().getId())
                .setClientId(mqRequest.getTransportableOrder().getClientId())
                .setConfirmator(mqRequest.getConfirmator())
                .setOrderRequestType(mqRequest.getOrderRequestType())
                .setConfirmStatus(OrderConfirmStatus.IN_PROGRESS);

        try {
            DeliveryDetails deliveryDetails = makeDeliveryDetails(mqRequest.getTransportableOrder().getDelivery());
            confirmRequest.setDelivery(deliveryDetails);
            confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED);

        } catch (ConflictException e) {
            confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED);
            confirmRejectCounter.increment();
        }

        if (mqRequest.getOrderRequestType() == OrderRequestType.REVERSE) {
            /* Поищем оригинальный запрос */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(mqRequest.getTransportableOrder().getId(), OrderConfirmStatus.CONFIRMED);
            if (requests.isEmpty()) { /* Если его нет*/
                confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED);
                confirmRejectCounter.increment();
            } else { /* Если он есть, помечаем его как REVERSED */
                requests.get(0).setConfirmStatus(OrderConfirmStatus.REVERSED);
                orderConfirmRequestRepo.saveAndFlush(requests.get(0));
            }
        }


        orderConfirmRequestRepo.saveAndFlush(confirmRequest);
        return confirmRequest;
    }

    private DeliveryDetails makeDeliveryDetails(MQDeliveryDetails delivery) throws ConflictException {

        DeliveryDetails deliveryDetails = new DeliveryDetails();

        if (delivery.getHomeDelivery() != null) {
            String companyName = delivery.getHomeDelivery().getCompanyName();
            Optional<Company> optionalCompany = companyRepo.findById(companyName);

            if (optionalCompany.isEmpty())
                throw new ConflictException("Не найдена компания " + companyName);

            HomeDelivery homeDelivery = new HomeDelivery()
                    .setCost(delivery.getHomeDelivery().getCost())
                    .setCompany(optionalCompany.get())
                    .setAddress(delivery.getHomeDelivery().getAddress());

            deliveryDetails.setHomeDelivery(homeDelivery);

        }

        if (delivery.getPickupPoint() != null) {


            long pickupPointId = delivery.getPickupPoint().getId();
            Optional<PickupPoint> optionalPickupPoint = pickupPointRepo.findById(pickupPointId);

            if (optionalPickupPoint.isEmpty())
                throw new ConflictException("Не найден пункт выдачи " + pickupPointId);

            deliveryDetails.setPickupPoint(optionalPickupPoint.get());
        }

        return deliveryDetails;
    }


    /* */
    @Transactional
    public RestOrderDeliveryCommand processOrder(RestOrderDeliveryCommand restOrderDeliveryCommandNotification) throws ConflictException {
        if (restOrderDeliveryCommandNotification.getOrderId() == null) {
            throw new ConflictException("OrderId not set");
        }

        List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderId(restOrderDeliveryCommandNotification.getOrderId());

        List<OrderConfirmRequest> pickedRequests = requests.stream().filter(o -> (o.getConfirmStatus() == OrderConfirmStatus.IN_PICKING)).toList();

        OrderConfirmRequest request;

        if (!pickedRequests.isEmpty()) {
            request = pickedRequests.get(0);

            if (request.getDelivery() == null)
                throw new ConflictException("Delivery not set for order");

            if (request.getDelivery().getHomeDelivery() != null) {
                if (restOrderDeliveryCommandNotification.getHourFrom() == null
                        || restOrderDeliveryCommandNotification.getHourTo() == null
                        || restOrderDeliveryCommandNotification.getDeliveryDate() == null) {
                    throw new ConflictException("deliveryDate, hourFrom, hourTo должны быть заданы для этого заказа");
                } else {
                    request.getDelivery().getHomeDelivery().setHourFrom(restOrderDeliveryCommandNotification.getHourFrom());
                    request.getDelivery().getHomeDelivery().setHourTo(restOrderDeliveryCommandNotification.getHourTo());
                    request.getDelivery().getHomeDelivery().setDeliveryDate(restOrderDeliveryCommandNotification.getDeliveryDate());
                }
            } else if (request.getDelivery().getPickupPoint() != null) {
                if (restOrderDeliveryCommandNotification.getDeliveryDate() == null) {
                    throw new ConflictException("deliveryDate должен быть задан для этого заказа");
                } else {
                    request.getDelivery().getPickupPoint().setDeliveryDate(restOrderDeliveryCommandNotification.getDeliveryDate());
                }
            }
            request.setConfirmStatus(OrderConfirmStatus.PROCESSED);

            orderConfirmRequestRepo.saveAndFlush(request);

        } else {
            List<OrderConfirmRequest> processedRequests = requests.stream().filter(o -> (o.getConfirmStatus() == OrderConfirmStatus.PROCESSED)).toList();
            if (processedRequests.isEmpty()) {
                throw new ConflictException("Не найден заказ на комплектации " + restOrderDeliveryCommandNotification.getOrderId());
            }

            request = processedRequests.get(0);
        }
        notificationService.sendOrderNotification(new MQDeliveryNotification()
                .setOrderId(restOrderDeliveryCommandNotification.getOrderId())
                .setClientId(request.getClientId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                .setDeliveryDetails(new MQDeliveryDetails(request.getDelivery())));

        return restOrderDeliveryCommandNotification;
    }

    @Transactional
    public void pickOrder(MQOrderNotification mqOrderNotification) {
        List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderId(mqOrderNotification.getOrderId());

        List<OrderConfirmRequest> confirmedRequests = requests.stream().filter(o -> (o.getConfirmStatus() == OrderConfirmStatus.CONFIRMED)).toList();

        if (!confirmedRequests.isEmpty()) {
            OrderConfirmRequest request = confirmedRequests.get(0);

            request.setConfirmStatus(OrderConfirmStatus.IN_PICKING);

            orderConfirmRequestRepo.saveAndFlush(request);

        } else {
            /* Тут инкрементируем метрику */
            pickingApplyingErrorCounter.increment();
        }
    }
}
