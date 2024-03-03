package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.DeliveryDetails;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQDeliveryDetails {
    private MQPickupPoint pickupPoint;
    private MQHomeDelivery homeDelivery;

    public MQDeliveryDetails(DeliveryDetails deliveryDetails){
        if (deliveryDetails.getPickupPoint() != null) {
            this.pickupPoint = new MQPickupPoint(deliveryDetails.getPickupPoint());
        }
        if (deliveryDetails.getHomeDelivery() != null) {
            this.homeDelivery = new MQHomeDelivery(deliveryDetails.getHomeDelivery());
        }
    }
}
