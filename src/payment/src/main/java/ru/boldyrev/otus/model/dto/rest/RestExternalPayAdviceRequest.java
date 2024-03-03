package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.PaymentResult;
import ru.boldyrev.otus.model.enums.TokenType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RestExternalPayAdviceRequest {
    private double amount;
    private Long internalPaymentId;
    private String orderId;
    private PaymentResult paymentResult;
    private TokenType tokenType;
    private String externalTokenId;
    private String externalPaymentId;
}
