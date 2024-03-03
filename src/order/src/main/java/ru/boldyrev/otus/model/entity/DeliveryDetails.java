package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.dto.mq.MQDeliveryDetails;
import ru.boldyrev.otus.model.dto.rest.RestDeliveryDetails;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Data
@Embeddable
@NoArgsConstructor
public class DeliveryDetails {
    @Embedded
    private PickupPoint pickupPoint;

    @Embedded
    private HomeDelivery homeDelivery;

    public DeliveryDetails(RestDeliveryDetails delivery) {
        if (delivery.getPickupPoint() != null) {
            this.pickupPoint = new PickupPoint(delivery.getPickupPoint());
        }
        if (delivery.getHomeDelivery() != null) {
            this.homeDelivery = new HomeDelivery(delivery.getHomeDelivery());
        }
    }
}
