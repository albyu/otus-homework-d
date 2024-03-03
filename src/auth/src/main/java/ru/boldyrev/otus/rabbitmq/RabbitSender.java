package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.NotifySerializationErrorCounter;
import ru.boldyrev.otus.metrics.PaymSerializationErrorCounter;
import ru.boldyrev.otus.model.transfer.TransportableUser;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RabbitSender {


    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;


    @Value("${application.rabbitmq.notification.exchangeName}")
    private String notificationExchange;

    @Value("${application.rabbitmq.account.exchangeName}")
    private String accountExchange;

    private final String notificationAuthRouteKey = "auth";

    private final String accountAuthRouteKey = "";

    private final PaymSerializationErrorCounter paymSerializationErrorCounter;

    private final NotifySerializationErrorCounter notifySerializationErrorCounter;


    private void sendRequest(TransportableUser tUser, String exchange, String routekey) throws JsonProcessingException {
        String tUserAsString = null;
        tUserAsString = objectMapper.writeValueAsString(tUser);
        rabbitTemplate.convertAndSend(exchange, routekey, tUserAsString);

    }


    public void sendAccountRequest(TransportableUser tUser) {
        try {
            sendRequest(tUser, accountExchange, accountAuthRouteKey);
        } catch (JsonProcessingException e) {
            log.error("Cannot send request for notification for username = {}", tUser.getUsername(), e);
            log.error("Error: ", e);
            paymSerializationErrorCounter.increment();
        }
    }


    public void sendNotificationRequest(TransportableUser tUser) {
        try {
            sendRequest(tUser, notificationExchange, notificationAuthRouteKey);
        } catch (JsonProcessingException e) {
            log.error("Cannot send request for notification for username = {}", tUser.getUsername(), e);
            log.error("Error: ", e);

            notifySerializationErrorCounter.increment();
        }


    }
}
