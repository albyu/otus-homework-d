package ru.boldyrev.otus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.boldyrev.otus.metrics.NotifySerializationErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmationAdvice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {

    @Value("${application.rabbitmq.notification.exchangeName}")
    private String notificationExchange;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final NotifySerializationErrorCounter notifySerializationErrorCounter;



    public void sendNotification(MQOrderConfirmationAdvice confirmationAdvice)  {
        String ttAsString = null;
        try {
            ttAsString = objectMapper.writeValueAsString(confirmationAdvice);
            rabbitTemplate.convertAndSend(notificationExchange, "orchestra", ttAsString);
        } catch (JsonProcessingException e) {
            log.error("Cannot send confirmation for payRequest.order = {}, username = {}, confirmation status = {} ",
                    confirmationAdvice.getOrderId(),
                    confirmationAdvice.getClientId(),
                    confirmationAdvice.getConfirmationStatus().getName(),  e);
            log.error("Error: ", e);
            notifySerializationErrorCounter.increment();
        }

    }
}
