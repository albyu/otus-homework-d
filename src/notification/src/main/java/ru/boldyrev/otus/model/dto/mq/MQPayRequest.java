package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class MQPayRequest {
    Long id;

    String clientId;

    String orderId;

    MQClientToken clientToken;

    double amount;

    PaymentResult paymentResult;

    PaymentGoal paymentGoal;

    Timestamp timestamp;

}