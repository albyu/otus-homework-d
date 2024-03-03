package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MQPickupPoint {
    private int id;
    private String name;
    private String address;
}
