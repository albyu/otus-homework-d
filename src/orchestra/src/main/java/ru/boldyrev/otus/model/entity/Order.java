package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.dto.mq.MQOrderItem;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;
import ru.boldyrev.otus.model.enums.OrderStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Data
@Accessors(chain = true)
@Entity
@Table(name = "ORDERS")
@NoArgsConstructor
public class Order {
    @Id
    private String id;

    private String clientId;

    // Добавляем поле status типа OrderStatus
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    private OrderSagaStatus sagaStatus;

    private int retryCount;

    @Embedded
    DeliveryDetails delivery;

    public Order(MQOrder transportableOrder){
        this.id = transportableOrder.getId();
        this.status = transportableOrder.getStatus();
        this.clientId = transportableOrder.getClientId();

        if (transportableOrder.getDelivery()!=null) {
            this.delivery = new DeliveryDetails(transportableOrder.getDelivery());
        }

        this.sagaStatus = OrderSagaStatus.NEW;
        this.retryCount = 0;

        this.orderItems = new HashSet<>();
        for (MQOrderItem transportableOrderItem : transportableOrder.getOrderItems()) {
            OrderItem orderItem = new OrderItem(transportableOrderItem);
            this.orderItems.add(orderItem);
        }
    }
}
