package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQDeliveryNotification {
    String clientId;
    String orderId;
    Timestamp timestamp;
    MQDeliveryDetails deliveryDetails;


}
