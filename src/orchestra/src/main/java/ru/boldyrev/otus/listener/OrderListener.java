package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.OrderParsingErrorCounter;
import ru.boldyrev.otus.metrics.OrderSerializationErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.OtusSystem;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.service.OrderService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Component
public class OrderListener {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final ConfirmListener confirmListener;

    private final OrderParsingErrorCounter orderParsingErrorCounter;

    private final OrderSerializationErrorCounter orderSerializationErrorCounter;

    @Value("${application.rabbitmq.orderres.exchangeName}")
    private String resOrderExchangeName;

    /* Отправка обработанного заказа в order-app */
    public void sendOrderRes(MQOrder mqOrder) {
        try {
            String orderAsString = null;
            orderAsString = objectMapper.writeValueAsString(mqOrder);
            rabbitTemplate.convertAndSend(resOrderExchangeName, "", orderAsString);
            log.info("Sent response {} for order {}", mqOrder.getStatus().getName(), mqOrder.getId());


        } catch (Exception e) {
            log.error("Cannot send response for order " + mqOrder.getId(), e);
            orderSerializationErrorCounter.increment();

        }
    }
    /* Обработка входящего сообщения от order-app */
    @RabbitListener(queues = "${application.rabbitmq.orderreq.queueName}")
    public void getOrderToProcess(String orderAsString) {
        MQOrder mqOrder;

        try {
            mqOrder = objectMapper.readValue(orderAsString, MQOrder.class);
            log.info("Received order {} for processing", mqOrder.getId());

            //Получаем или заводим новый кейс
            Order order = orderService.registerOrder(mqOrder);
            orderService.saveOrderAddLogEntry(order, "OrderCase created");

            /*Проверим статус кейса*/
            if (order.getSagaStatus() == OrderSagaStatus.NEW || /*Новый кейс*/
                    order.getSagaStatus() == OrderSagaStatus.FAILED /*Ранее зафейленный кейс*/

            ) {

                order.setSagaStatus(OrderSagaStatus.STARTED);
                orderService.saveOrderAddLogEntry(order, "Payment confirmation requested");

                /* Создаем запрос для кейса - прикрепляем заказ, id кейса, тип запроса (CONFIRM) */
                MQOrderConfirmRequest orderConfirmRequest = new MQOrderConfirmRequest(order, OrderRequestType.CONFIRM, OtusSystem.PAYMENT);

                /*Посылаем запрос в Payment*/
                confirmListener.sendOrderToConfirm(orderConfirmRequest, ConfirmListener.PAYMENT_REQ_ROUTEKEY);
            }
            /* Если кейс уже выполнен успешно */
            else if (order.getSagaStatus() == OrderSagaStatus.COMPLETED) {
                MQOrder orderToReturn = new MQOrder(order);
                sendOrderRes(orderToReturn);
            }
            /* Иначе ничего не делаем - ожидаем завершения обработки */

        } catch (Exception e) {
            log.error("Error in order processing", e);

            orderParsingErrorCounter.increment();
        }

    }


}
