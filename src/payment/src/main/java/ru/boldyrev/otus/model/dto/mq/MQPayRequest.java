package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.dto.rest.RestClientToken;
import ru.boldyrev.otus.model.entity.PayRequest;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class MQPayRequest {

    Long id;

    String clientId;

    String orderId;

    RestClientToken clientToken;

    double amount;

    PaymentResult paymentResult;

    PaymentGoal paymentGoal;

    Timestamp timestamp;

    public MQPayRequest(PayRequest payRequest){
        this.id = payRequest.getId();
        this.clientId = payRequest.getClientId();
        this.orderId = payRequest.getOrderId();
        this.clientToken = new RestClientToken(payRequest.getClientToken());
        this.amount = payRequest.getAmount();
        this.paymentResult = payRequest.getPaymentResult();
        this.paymentGoal = payRequest.getPaymentGoal();
        this.timestamp = payRequest.getTimestamp();
    }


}
