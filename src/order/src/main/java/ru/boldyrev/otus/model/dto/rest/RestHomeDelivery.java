package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.HomeDelivery;
import ru.boldyrev.otus.model.entity.PickupPoint;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestHomeDelivery {
    private String companyName;
    private double cost;
    private String address;

    public RestHomeDelivery(HomeDelivery homeDelivery) {
        this.companyName = homeDelivery.getCompanyName();
        this.cost = homeDelivery.getCost();
        this.address = homeDelivery.getAddress();
    }
}
