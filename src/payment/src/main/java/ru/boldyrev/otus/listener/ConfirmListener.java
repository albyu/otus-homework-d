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
import ru.boldyrev.otus.metrics.ConfirmParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmResponse;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;
import ru.boldyrev.otus.model.enums.OtusSystem;
import ru.boldyrev.otus.service.ConfirmService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ConfirmListener {


    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final ConfirmService confirmService;

    private final ConfirmParsingErrorCounter confirmParsingErrorCounter;

    @Value("${application.role}")
    private OtusSystem otusSystem;

    @Value("${application.rabbitmq.confirmres.exchangeName}")
    private String resConfirmExchangeName;


    public void sendConfirmRes(MQOrderConfirmResponse response) throws JsonProcessingException {
        String responseAsString = null;
        responseAsString = objectMapper.writeValueAsString(response);
        rabbitTemplate.convertAndSend(resConfirmExchangeName, "", responseAsString);
    }


    /*Получение запроса на подтверждение заказа*/
    @RabbitListener(queues = "${application.rabbitmq.confirmreq.paymentQueueName}")
    public void receiveMessage(String requestAsString) {
        MQOrderConfirmRequest mqRequest;

        try {
            mqRequest = objectMapper.readValue(requestAsString, MQOrderConfirmRequest.class);
            log.info("confirmation request for order {} received", mqRequest.getTransportableOrder().getId());


            /* Зарегистрировать запрос */
            OrderConfirmRequest orderConfirmRequest = confirmService.register(mqRequest);


            //Формируем ответное сообщение
            MQOrderConfirmResponse response
                    = new MQOrderConfirmResponse()
                    .setOrderId(orderConfirmRequest.getOrderId())
                    .setRequestType(orderConfirmRequest.getOrderRequestType())
                    .setConfirmator(orderConfirmRequest.getConfirmator())
                    .setOrderConfirmStatus(orderConfirmRequest.getConfirmStatus())
                    .setErrorMessage(orderConfirmRequest.getErrorMessage())
                    .setReferenceId(orderConfirmRequest.getPayRequest()!=null ? orderConfirmRequest.getPayRequest().getId().toString(): null);


            //Отправляем ответное сообщение
            sendConfirmRes(response);
            log.info("confirmation response for order {} sent, result {}", response.getOrderId(), response.getOrderConfirmStatus().getName());


        } catch (Exception e) {
            log.error("Cannot process " + requestAsString, e);
            confirmParsingErrorCounter.increment();
        }
    }



}
