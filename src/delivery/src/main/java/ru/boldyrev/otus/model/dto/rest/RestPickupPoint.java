package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.PickupPoint;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestPickupPoint {
    Long id;
    String name;
    String address;

    public RestPickupPoint(PickupPoint pickupPoint){
        this.id = pickupPoint.getId();
        this.name = pickupPoint.getName();
        this.address = pickupPoint.getAddress();
    }
}
