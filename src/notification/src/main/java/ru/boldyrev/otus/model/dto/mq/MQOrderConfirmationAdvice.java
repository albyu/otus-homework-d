package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;
import ru.boldyrev.otus.model.enums.OrderStatus;

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

}
