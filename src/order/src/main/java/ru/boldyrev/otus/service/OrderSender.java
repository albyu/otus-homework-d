package ru.boldyrev.otus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.model.dto.mq.MQOrder;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderSender {
    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    @Value("${application.rabbitmq.orderreq.exchangeName}")
    private String reqOrderExchangeName;

    public void sendMessageReq(MQOrder order) throws JsonProcessingException {
        String orderAsString = null;
        orderAsString = objectMapper.writeValueAsString(order);
        rabbitTemplate.convertAndSend(reqOrderExchangeName, "", orderAsString);
    }

}
