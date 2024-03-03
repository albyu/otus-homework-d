package ru.boldyrev.otus.model.dto.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;


@Data
@Accessors(chain = true)
public class MQPickupPoint {
    private int id;
    private String name;
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    LocalDate deliveryDate; /* Срок доставки */
}
