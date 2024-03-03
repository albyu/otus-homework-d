package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderStatus;

import javax.persistence.*;
import java.util.Set;


@Data
@Accessors(chain = true)
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    private String id;

    @Version
    private Long version;

    /* Владелец заказа */
    private String clientId;

    // Добавляем поле status типа OrderStatus
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems;

    @Embedded
    private DeliveryDetails delivery;

}



