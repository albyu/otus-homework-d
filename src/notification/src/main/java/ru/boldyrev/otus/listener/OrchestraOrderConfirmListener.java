package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.OrchestraNotifyParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmationAdvice;
import ru.boldyrev.otus.service.NotificationService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class OrchestraOrderConfirmListener {

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final OrchestraNotifyParsingErrorCounter orchestraNotifyParsingErrorCounter;

    /* Получение запроса на уведомление о результатате согласования заказа */
    @RabbitListener(queues = "${application.rabbitmq.notification.orchestraQueueName}")
    public void receiveMessageFromOrchestra(String tPayConfirmAsString) {
        MQOrderConfirmationAdvice tPayConfirm;

        try {
            /**/
            tPayConfirm = objectMapper.readValue(tPayConfirmAsString, MQOrderConfirmationAdvice.class);
            log.info("Notification request for confirmation notification for order {} received", tPayConfirm.getOrderId());

            notificationService.createOrderConfirmNotification(tPayConfirm);

        } catch (Exception e) {
            log.error("Cannot process " + tPayConfirmAsString, e);
            orchestraNotifyParsingErrorCounter.increment();
        }
    }
}
