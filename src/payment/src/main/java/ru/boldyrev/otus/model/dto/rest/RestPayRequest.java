package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.entity.ClientToken;
import ru.boldyrev.otus.model.entity.PayRequest;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class RestPayRequest {

    private Long id;

    private String clientId;

    private String orderId;

    private RestClientToken clientToken;

    private double amount;

    private PaymentResult paymentResult;

    private PaymentGoal paymentGoal;

    private Timestamp timestamp;

    private String externalPaymentId;

    public RestPayRequest(PayRequest payRequest){
        this.id = payRequest.getId();
        this.clientId = payRequest.getClientId();
        this.orderId = payRequest.getOrderId();
        this.clientToken = new RestClientToken(payRequest.getClientToken());
        this.amount = payRequest.getAmount();
        this.paymentResult = payRequest.getPaymentResult();
        this.paymentGoal = payRequest.getPaymentGoal();
        this.timestamp = payRequest.getTimestamp();
        this.externalPaymentId = payRequest.getExternalPaymentId();
    }


}
