package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import ru.boldyrev.otus.model.enums.PaymentResult;

@Data
public class RestPayResponse {
    String externalPayLink;
    PaymentResult paymentResult;
    RestClientToken clientToken;
    String orderId;
    Long paymentRequestId;
}
