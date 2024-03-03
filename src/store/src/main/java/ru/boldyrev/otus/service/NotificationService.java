package ru.boldyrev.otus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.metrics.OrderPickingNotifySerializationErrorCounter;
import ru.boldyrev.otus.metrics.ProductNotifySerializationErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrderNotification;
import ru.boldyrev.otus.model.dto.mq.MQProduct;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    @Value("${application.rabbitmq.orderentity.exchangeName}")
    private String orderExchange;

    @Value("${application.rabbitmq.delivery.exchangeName}")
    private String deliveryExchange;


    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final OrderPickingNotifySerializationErrorCounter orderPickingNotifySerializationErrorCounter;

    private final ProductNotifySerializationErrorCounter productNotifySerializationErrorCounter;


    /* Уведомление о создании/изменении продукта */
    public void sendProductNotification(MQProduct product)  {
        String ttAsString = null;
        try {
            ttAsString = objectMapper.writeValueAsString(product);
            rabbitTemplate.convertAndSend(orderExchange, "", ttAsString);
        } catch (JsonProcessingException e) {
            log.error("Cannot send notify for payRequest.order = {}", product.getId(), e);
            log.error("Error: ", e);
            productNotifySerializationErrorCounter.increment();
        }

    }

    /* Уведомление о готовности заказа на складе */
    public void sendOrderNotification(MQOrderNotification orderNotification) {
        String ttAsString = null;
        try {
            ttAsString = objectMapper.writeValueAsString(orderNotification);
            rabbitTemplate.convertAndSend(deliveryExchange, "", ttAsString);
        } catch (JsonProcessingException e) {
            log.error("Cannot send notify for payRequest.order = {}", orderNotification.getOrderId(), e);
            log.error("Error: ", e);
            orderPickingNotifySerializationErrorCounter.increment();
        }

    }
}
