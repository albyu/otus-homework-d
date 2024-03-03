package ru.boldyrev.otus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.metrics.NotifySerializationErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQDeliveryNotification;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    @Value("${application.rabbitmq.notification.exchangeName}")
    private String notificationExchange;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final NotifySerializationErrorCounter notifySerializationErrorCounter;


        /* Уведомление о готовности заказа на складе */
        public void sendOrderNotification(MQDeliveryNotification orderNotification) {
        String ttAsString = null;
        try {
            ttAsString = objectMapper.writeValueAsString(orderNotification);
            rabbitTemplate.convertAndSend(notificationExchange, "delivery", ttAsString);
        } catch (JsonProcessingException e) {
            log.error("Cannot send notify for order = {}", orderNotification.getOrderId(), e);
            log.error("Error: ", e);
            notifySerializationErrorCounter.increment();
        }

    }
}
