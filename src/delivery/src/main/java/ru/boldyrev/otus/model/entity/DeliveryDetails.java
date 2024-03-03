package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.ManyToOne;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@Accessors(chain = true)
@Embeddable
public class DeliveryDetails {
    @ManyToOne
    private PickupPoint pickupPoint;

    @Embedded
    private HomeDelivery homeDelivery;



}
