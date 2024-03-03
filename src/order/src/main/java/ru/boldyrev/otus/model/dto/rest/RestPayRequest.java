package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.PayResult;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RestPayRequest {
    Long id;

    String username;

    String orderId;

    double amount;

    PayResult payResult;

    Long transactionId;

    Timestamp timestamp;

}

