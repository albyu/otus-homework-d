package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.dto.mq.MQHomeDelivery;
import ru.boldyrev.otus.model.dto.rest.RestHomeDelivery;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
@NoArgsConstructor
public class HomeDelivery {
    @Column(name = "home_delivery_company_name")
    private String companyName;

    @Column(name = "home_delivery_cost")
    private double cost;

    @Column(name = "home_delivery_address")
    private String address;

    public HomeDelivery(RestHomeDelivery homeDelivery) {
        this.companyName = homeDelivery.getCompanyName();
        this.cost = homeDelivery.getCost();
        this.address = homeDelivery.getAddress();
    }

    public HomeDelivery(MQHomeDelivery homeDelivery) {
        this.companyName = homeDelivery.getCompanyName();
        this.cost = homeDelivery.getCost();
        this.address = homeDelivery.getAddress();
    }
}