package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderStatus;
import ru.boldyrev.otus.model.enums.PaymentResult;

@Data
@Accessors(chain = true)
public class RestExternalPayAdviceResponse {
    PaymentResult finalPaymentResult;
    private String externalPaymentId;
    private Long internalPaymentId;
    private String orderId;

}
