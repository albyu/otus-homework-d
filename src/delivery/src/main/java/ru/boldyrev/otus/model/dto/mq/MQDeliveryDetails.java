package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.DeliveryDetails;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQDeliveryDetails {
    private MQPickupPoint pickupPoint;
    private MQHomeDelivery homeDelivery;

    public MQDeliveryDetails(DeliveryDetails delivery){

        if (delivery.getHomeDelivery() != null) {

            this.homeDelivery = new MQHomeDelivery(delivery.getHomeDelivery());
        }

        if (delivery.getPickupPoint() != null) {

            this.pickupPoint = new MQPickupPoint(delivery.getPickupPoint());
        }
    }
}
