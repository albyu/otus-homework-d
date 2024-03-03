package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.PaymentNotifyParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQPayRequest;
import ru.boldyrev.otus.service.NotificationService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PaymentNotificationListener {

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    private final PaymentNotifyParsingErrorCounter paymentNotifyParsingErrorCounter;

    /* Получение запроса на уведомление о попытке совершения платежа */
    @RabbitListener(queues = "${application.rabbitmq.notification.accQueueName}")
    public void receiveMessageFromPayment(String tPayReqAsString) {
        MQPayRequest tPayReq = new MQPayRequest();

        try {
            /**/
            tPayReq = objectMapper.readValue(tPayReqAsString, MQPayRequest.class);
            log.info("Notification request for payment of order {} received", tPayReq.getOrderId());

            notificationService.createPaymentNotification(tPayReq);

        } catch (Exception e) {
            log.error("Cannot process " + tPayReqAsString, e);

            paymentNotifyParsingErrorCounter.increment();
        }
    }
}
