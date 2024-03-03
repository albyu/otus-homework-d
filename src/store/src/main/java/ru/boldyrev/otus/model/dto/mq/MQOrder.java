package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderStatus;

import java.util.Set;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQOrder {

    private String id;

    private OrderStatus status;

    private Set<MQOrderItem> orderItems;

    private MQDeliveryDetails delivery;

}
