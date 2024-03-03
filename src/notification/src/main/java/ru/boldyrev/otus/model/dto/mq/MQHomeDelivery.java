package ru.boldyrev.otus.model.dto.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQHomeDelivery {
    private String companyName;
    private double cost;
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    LocalDate deliveryDate; /* Срок доставки */

    int hourFrom; /* Интервал доставки час от */
    int hourTo; /* Интервал доставки час до */
}
