package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.entity.OrderItem;
import ru.boldyrev.otus.model.enums.OrderStatus;

import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class  MQOrder {

    private String clientId;

    private String id;

    private OrderStatus status;

    private Set<MQOrderItem> orderItems;

    private MQDeliveryDetails delivery;



    public MQOrder(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.clientId = order.getClientId();
        this.orderItems = new HashSet<>();
        for (OrderItem item : order.getOrderItems()) {
            MQOrderItem transportableOrderItem = new MQOrderItem(item);
            this.orderItems.add(transportableOrderItem);
        }

        if (order.getDelivery()!=null) {
            this.delivery = new MQDeliveryDetails(order.getDelivery());
        }

    }
}
