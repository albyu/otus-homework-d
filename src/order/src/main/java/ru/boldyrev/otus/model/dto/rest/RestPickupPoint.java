package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.PickupPoint;

@Data
@Accessors(chain = true)
public class RestPickupPoint {
    private int id;
    private String name;
    private String address;

    public RestPickupPoint(PickupPoint pickupPoint) {
        this.id = pickupPoint.getId();
        this.name = pickupPoint.getName();
        this.address = pickupPoint.getAddress();
    }
}
