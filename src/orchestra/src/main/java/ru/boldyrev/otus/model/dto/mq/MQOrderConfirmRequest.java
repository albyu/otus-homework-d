package ru.boldyrev.otus.model.dto.mq;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.OtusSystem;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MQOrderConfirmRequest {
    MQOrder transportableOrder;

    OrderRequestType orderRequestType;

    OtusSystem confirmator;

    public MQOrderConfirmRequest(Order order, OrderRequestType orderRequestType, OtusSystem otusSystem) {
        this.transportableOrder = new MQOrder(order);
        this.orderRequestType = orderRequestType;
        this.confirmator = otusSystem;
    }
}
