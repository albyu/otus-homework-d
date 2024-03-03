package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.AuthNotifyParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQClient;
import ru.boldyrev.otus.service.NotificationService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuthNewClientListener {

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final AuthNotifyParsingErrorCounter authNotifyParsingErrorCounter;

    /* Получение запроса о создании новой пользовательской записи */
    @RabbitListener(queues = "${application.rabbitmq.notification.authQueueName}")
    public void receiveMessageFromAuth(String tUserAsString) {
        MQClient tUser = new MQClient();

        try {
            tUser = objectMapper.readValue(tUserAsString, MQClient.class);
            log.info("Request for registration of user {} received", tUser.getUsername());

            notificationService.saveUser(tUser);

        } catch (Exception e) {
            log.error("Cannot process " + tUserAsString, e);
            authNotifyParsingErrorCounter.increment();
        }
    }
}
