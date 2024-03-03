package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.HomeDelivery;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQHomeDelivery {
    private String companyName;
    private double cost;
    private String address;

    public MQHomeDelivery(HomeDelivery homeDelivery) {
        this.companyName = homeDelivery.getCompanyName();
        this.cost = homeDelivery.getCost();
        this.address = homeDelivery.getAddress();
    }
}
