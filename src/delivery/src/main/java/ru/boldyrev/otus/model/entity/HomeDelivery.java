package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Embeddable
public class HomeDelivery {
    @ManyToOne
    private Company company;
    private double cost;
    private String address;

    LocalDate deliveryDate; /* Срок доставки */
    int hourFrom; /* Интервал доставки час от */
    int hourTo; /* Интервал доставки час до */
}
