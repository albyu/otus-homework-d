package ru.boldyrev.otus.model.dto.mq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.PickupPoint;

import java.time.LocalDate;


@Data
@Accessors(chain = true)
public class MQPickupPoint {
    private Long id;
    private String name;
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate deliveryDate;

    public MQPickupPoint (PickupPoint pickupPoint){
        this.id = pickupPoint.getId();
        this.name = pickupPoint.getName();
        this.address = pickupPoint.getAddress();

        this.deliveryDate = pickupPoint.getDeliveryDate();
    }
}
