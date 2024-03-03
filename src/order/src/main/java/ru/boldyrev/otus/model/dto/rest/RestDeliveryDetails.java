package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.DeliveryDetails;


@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestDeliveryDetails {
    private RestPickupPoint pickupPoint;
    private RestHomeDelivery homeDelivery;

    public RestDeliveryDetails(DeliveryDetails deliveryDetails) {
        if (deliveryDetails.getPickupPoint() != null)
            this.pickupPoint = new RestPickupPoint(deliveryDetails.getPickupPoint());
        if (deliveryDetails.getHomeDelivery() != null)
            this.homeDelivery = new RestHomeDelivery(deliveryDetails.getHomeDelivery());

    }

}
