package ru.boldyrev.otus.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.PickingParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrderNotification;
import ru.boldyrev.otus.service.ConfirmService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ExecutionListener {
    private final ObjectMapper objectMapper;

    private final ConfirmService confirmService;

    private final PickingParsingErrorCounter pickingParsingErrorCounter;

    @RabbitListener(queues = "${application.rabbitmq.delivery.queueName}")
    public void receiveMessage(String requestAsString) {
        MQOrderNotification mqOrderNotification;

        try {
            mqOrderNotification = objectMapper.readValue(requestAsString, MQOrderNotification.class);
            log.info("order {} received", mqOrderNotification.getOrderId());

            confirmService.pickOrder(mqOrderNotification);

        } catch (Exception e) {
            log.error("Cannot process " + requestAsString, e);
            pickingParsingErrorCounter.increment();
        }
    }
}
