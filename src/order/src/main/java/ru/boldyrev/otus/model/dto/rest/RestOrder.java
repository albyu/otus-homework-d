package ru.boldyrev.otus.model.dto.rest;

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
public class RestOrder {


    private String id;

    private Long version;

    private OrderStatus status;

    private Set<RestOrderItem> orderItems;

    private RestDeliveryDetails delivery;

    public RestOrder(Order order) {
        this.id = order.getId();
        this.version = order.getVersion();
        this.status = order.getStatus();
        this.orderItems = new HashSet<>();
        for (OrderItem item : order.getOrderItems()) {
            RestOrderItem restOrderItem = new RestOrderItem(item);
            this.orderItems.add(restOrderItem);
        }
        if (order.getDelivery() != null){
            this.delivery = new RestDeliveryDetails(order.getDelivery());
        }
    }
}
