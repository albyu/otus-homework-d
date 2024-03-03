package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestOrderNotification {
    String orderId;
    Timestamp timestamp;
}
