package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.PickupPoint;

import javax.persistence.Column;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQPickupPoint {
    private int id;
    private String name;
    private String address;

    public MQPickupPoint(PickupPoint pickupPoint) {
        this.id = pickupPoint.getId();
        this.name = pickupPoint.getName();
        this.address = pickupPoint.getAddress();
    }
}
