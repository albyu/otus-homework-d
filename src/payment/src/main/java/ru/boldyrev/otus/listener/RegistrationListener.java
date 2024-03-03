package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.RegistrationParsingErrorCounter;
import ru.boldyrev.otus.model.dto.rest.RestPayRequest;
import ru.boldyrev.otus.model.dto.mq.MQClient;
import ru.boldyrev.otus.service.RegistrationService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RegistrationListener {

    private final ObjectMapper objectMapper;

    private final RegistrationService registrationService;

    private final RegistrationParsingErrorCounter registrationParsingErrorCounter;


    @Value("${application.rabbitmq.notification.exchangeName}")
    private String notificationExchange;


    /*Получение запроса на создание клиента и счета*/
    @RabbitListener(queues = "${application.rabbitmq.account.queueName}")
    public void receiveAccountOpeningRequest(String mqClientAsString) {
        MQClient mqClient;
        try {
            mqClient = objectMapper.readValue(mqClientAsString, MQClient.class);
            log.info("request for opening for user {} received", mqClient.getClientId());

            /* Регистрируем нового клиента, токен, счет */
            registrationService.register(mqClient);

        } catch (Exception e) {
            log.error("Cannot process {}", mqClientAsString);
            log.error("Error: ", e);
            registrationParsingErrorCounter.increment();
        }
    }
}
