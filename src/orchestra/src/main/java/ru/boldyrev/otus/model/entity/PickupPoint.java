package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.dto.mq.MQPickupPoint;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
@NoArgsConstructor
public class PickupPoint {

    @Column(name = "pickup_point_id")
    private int id;

    @Column(name = "pickup_point_name")
    private String name;

    @Column(name = "pickup_point_address")
    private String address;

    public PickupPoint(MQPickupPoint pickupPoint) {
        this.id = pickupPoint.getId();
        this.name = pickupPoint.getName();
        this.address = pickupPoint.getAddress();
    }
}