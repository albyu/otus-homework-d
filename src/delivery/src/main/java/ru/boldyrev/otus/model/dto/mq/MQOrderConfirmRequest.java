package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.OtusSystem;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MQOrderConfirmRequest {
    MQOrder transportableOrder;
    OrderRequestType orderRequestType;
    OtusSystem confirmator;

}
