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
@Accessors(chain = true)
@NoArgsConstructor
public class RestOrder {

    private String id;

    private OrderStatus status;

    private Set<RestOrderItem> orderItems;

    public RestOrder(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.orderItems = new HashSet<>();
        for (OrderItem item : order.getOrderItems()) {
            RestOrderItem transportableOrderItem = new RestOrderItem(item);
            this.orderItems.add(transportableOrderItem);
        }
    }
}
