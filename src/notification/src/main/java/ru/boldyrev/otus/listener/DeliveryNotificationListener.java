package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.DeliveryNotifyParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQDeliveryNotification;
import ru.boldyrev.otus.service.NotificationService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DeliveryNotificationListener {

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final DeliveryNotifyParsingErrorCounter deliveryNotifyParsingErrorCounter;

    /* Получение запроса на уведомление о результатате согласования заказа */
    @RabbitListener(queues = "${application.rabbitmq.notification.deliveryQueueName}")
    public void receiveMessageFromDelivery(String tDeliveryNotificationAsString) {
        MQDeliveryNotification tDeliveryMessage;

        try {
            /**/
            tDeliveryMessage = objectMapper.readValue(tDeliveryNotificationAsString, MQDeliveryNotification.class);
            log.info("Notification request for delivery notification for order {} received", tDeliveryMessage.getOrderId());

            notificationService.createDeliveryMessageNotification(tDeliveryMessage);

        } catch (Exception e) {
            log.error("Cannot process " + tDeliveryNotificationAsString, e);
            deliveryNotifyParsingErrorCounter.increment();
        }
    }

}
