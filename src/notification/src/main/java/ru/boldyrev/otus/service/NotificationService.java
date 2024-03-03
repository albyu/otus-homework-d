package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.NotFoundException;
import ru.boldyrev.otus.metrics.DeliveryNotifyParsingErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQDeliveryNotification;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmationAdvice;
import ru.boldyrev.otus.model.entity.Notification;
import ru.boldyrev.otus.model.entity.User;
import ru.boldyrev.otus.model.enums.NotificationType;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;
import ru.boldyrev.otus.model.enums.PaymentResult;
import ru.boldyrev.otus.model.dto.rest.RestNotification;
import ru.boldyrev.otus.model.dto.mq.MQPayRequest;
import ru.boldyrev.otus.model.dto.mq.MQClient;
import ru.boldyrev.otus.model.enums.TokenType;
import ru.boldyrev.otus.repo.NotificationRepo;
import ru.boldyrev.otus.repo.UserRepo;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final UserRepo userRepo;

    private final NotificationRepo notificationRepo;

    private final DeliveryNotifyParsingErrorCounter deliveryNotifyParsingErrorCounter;

    private String formatMessage(Timestamp ts) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = new Date(ts.getTime());
        return dateFormat.format(date);
    }

    @Transactional
    public void createPaymentNotification(MQPayRequest tPayReq) {
        String notificationText;
        if (tPayReq.getPaymentResult() == PaymentResult.SUCCESS) { /* Письмо счастья */
            notificationText =
                    String.format("Удачно оплачен заказ %s на сумму %f, дата/время: %s. Цель оплаты: %s. Платежное средство: %s",
                            tPayReq.getOrderId(),
                            tPayReq.getAmount(),
                            formatMessage(tPayReq.getTimestamp()),
                            tPayReq.getPaymentGoal().getName(),
                            tPayReq.getClientToken().getTokenType() == TokenType.WALLET ? "средства кошелька" : tPayReq.getClientToken().getTokenType().getName() + " " + tPayReq.getClientToken().getExternalTokenId()
                    );
        } else { /* Письмо горя */
            notificationText =
                    String.format("Ошибка оплаты заказа %s на сумму %f. Причина: %s, дата/время: %s. Цель оплаты: %s. Платежное средство: %s",
                            tPayReq.getOrderId(), tPayReq.getAmount(),
                            tPayReq.getPaymentResult().getName(),
                            formatMessage(tPayReq.getTimestamp()),
                            tPayReq.getPaymentGoal().getName(),
                            tPayReq.getClientToken().getTokenType() == TokenType.WALLET ? "средства кошелька" : tPayReq.getClientToken().getTokenType().getName() + " " + tPayReq.getClientToken().getExternalTokenId()
                    );
        }

        /* Ищем пользователя, чтобы достать e-mail*/
        Notification note = new Notification()
                .setClientId(tPayReq.getClientId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                .setText(notificationText).setNotificationType(NotificationType.PAYMENT_RESULT);

        Optional<User> optionalUser = userRepo.findByUsername(tPayReq.getClientId());
        if (optionalUser.isPresent()) { /* Пользователь существует */
            note.setIsDeadNotification(Boolean.FALSE)
                    .setEmail(optionalUser.get().getEmail());
            /* Тут отправка e-mail*/

        } else { /*Пользователь не существует*/
            note.setIsDeadNotification(Boolean.TRUE);
            deliveryNotifyParsingErrorCounter.increment();
        }

        notificationRepo.saveAndFlush(note);
    }

    @Transactional
    public void createOrderConfirmNotification(MQOrderConfirmationAdvice tPayConfirm) {
        String notificationText;
        if (tPayConfirm.getConfirmationStatus() == OrderSagaStatus.COMPLETED) { /* Письмо счастья */
            notificationText =
                    String.format("Заказ %s одобрен",
                            tPayConfirm.getOrderId()
                    );
        } else { /* Письмо горя */
            notificationText =
                    String.format("Ошибка согласования заказа %s",
                            tPayConfirm.getOrderId()
                    );
        }

        /* Ищем пользователя, чтобы достать e-mail*/
        Notification note = new Notification()
                .setClientId(tPayConfirm.getClientId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                .setText(notificationText).setNotificationType(NotificationType.PAYMENT_RESULT);

        Optional<User> optionalUser = userRepo.findByUsername(tPayConfirm.getClientId());
        if (optionalUser.isPresent()) { /* Пользователь существует */
            note.setIsDeadNotification(Boolean.FALSE)
                    .setEmail(optionalUser.get().getEmail());
            /* Тут отправка e-mail*/

        } else { /*Пользователь не существует*/
            note.setIsDeadNotification(Boolean.TRUE);
            deliveryNotifyParsingErrorCounter.increment();
        }

        notificationRepo.saveAndFlush(note);
    }

    @Transactional
    public void createDeliveryMessageNotification(MQDeliveryNotification tDeliveryMessage) {
        String notificationText;
        /* Письмо счастья */
        notificationText =
                String.format("Заказ %s будет доставлен ",
                        tDeliveryMessage.getOrderId()
                );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (tDeliveryMessage.getDeliveryDetails() != null)
            if (tDeliveryMessage.getDeliveryDetails().getPickupPoint() != null) {
                notificationText += String.format("на пункт выдачи %s по адресу %s. Дата: %s",
                        tDeliveryMessage.getDeliveryDetails().getPickupPoint().getName(),
                        tDeliveryMessage.getDeliveryDetails().getPickupPoint().getAddress(),
                        tDeliveryMessage.getDeliveryDetails().getPickupPoint().getDeliveryDate().format(formatter)
                );
            } else if (tDeliveryMessage.getDeliveryDetails().getHomeDelivery() != null) {
                notificationText += String.format("по адресу %s. Дата: %s, c %d по %d. Компания: %s. Стоимость доставки: %f",
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getAddress(),
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getDeliveryDate().format(formatter),
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getHourFrom(),
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getHourTo(),
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getCompanyName(),
                        tDeliveryMessage.getDeliveryDetails().getHomeDelivery().getCost()
                );
            }



        /* Ищем пользователя, чтобы достать e-mail*/
        Notification note = new Notification()
                .setClientId(tDeliveryMessage.getClientId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()))
                .setText(notificationText).setNotificationType(NotificationType.PAYMENT_RESULT);

        Optional<User> optionalUser = userRepo.findByUsername(tDeliveryMessage.getClientId());
        if (optionalUser.isPresent()) { /* Пользователь существует */
            note.setIsDeadNotification(Boolean.FALSE)
                    .setEmail(optionalUser.get().getEmail());
            /* Тут отправка e-mail*/

        } else { /*Пользователь не существует*/
            note.setIsDeadNotification(Boolean.TRUE);
            deliveryNotifyParsingErrorCounter.increment();
        }

        notificationRepo.saveAndFlush(note);

    }

    @Transactional
    public void saveUser(MQClient tUser) {

        User user = userRepo.findByUsername(tUser.getUsername())  /* Существующий пользователь */
                .orElse(new User().setUsername(tUser.getUsername())); /* Новый пользователь */
        /* e-mail в любом случае обновляем */
        user.setEmail(tUser.getEmail());
        userRepo.saveAndFlush(user);
    }


    public List<RestNotification> getNotifications(String clientId) throws NotFoundException {
        if (userRepo.existsById(clientId)) {
            return notificationRepo.findByClientId(clientId).stream().map(RestNotification::new).toList();
        } else throw new NotFoundException("User not found");
    }


}
