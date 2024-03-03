package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.Notification;
import ru.boldyrev.otus.model.enums.PaymentResult;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RestNotification {

    Long id;

    String email;

    String orderId;

    String clientId;

    double amount;

    PaymentResult paymentResult;

    Timestamp timestamp;

    String text;

    Boolean isDeadNotification;

    public RestNotification(Notification note){
        this.id = note.getId();
        this.clientId = note.getClientId();
        this.email = note.getEmail();
        this.timestamp = note.getTimestamp();
        this.text = note.getText();
        this.isDeadNotification = note.getIsDeadNotification();
    }

}
