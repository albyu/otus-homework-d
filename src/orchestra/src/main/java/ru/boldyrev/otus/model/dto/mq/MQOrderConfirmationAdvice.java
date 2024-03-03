package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.entity.OrderItem;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;
import ru.boldyrev.otus.model.enums.OrderStatus;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class MQOrderConfirmationAdvice {

    private String clientId;

    private String orderId;

    private OrderStatus status;

    private Set<MQOrderItem> orderItems;

    private MQDeliveryDetails delivery;

    private OrderSagaStatus confirmationStatus;

    public MQOrderConfirmationAdvice(Order order) {
        this.orderId = order.getId();
        this.status = order.getStatus();
        this.clientId = order.getClientId();

        this.orderItems = new HashSet<>();

        for (OrderItem item : order.getOrderItems()) {
            MQOrderItem transportableOrderItem = new MQOrderItem(item);
            this.orderItems.add(transportableOrderItem);
        }

        if (order.getDelivery() != null) {
            this.delivery = new MQDeliveryDetails(order.getDelivery());
        }
        this.confirmationStatus = order.getSagaStatus();

    }



}
