package ru.boldyrev.otus.model.dto.rest;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.OrderLogEntry;

import java.sql.Timestamp;


@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestLogEntry {

    private Long id;

    private String orderId;

    private String message;

    private Timestamp timestamp;

    public RestLogEntry(OrderLogEntry orderCaseLogEntry){
        this.id = orderCaseLogEntry.getId();
        this.orderId = orderCaseLogEntry.getOrder().getId();
        this.message = orderCaseLogEntry.getMessage();
        this.timestamp = orderCaseLogEntry.getTimestamp();
    }
}