package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.model.dto.mq.MQOrderItem;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.OtusSystem;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@Data
@Accessors(chain = true)
@Entity
@Table(name = "order_confirm_requests")
public class OrderConfirmRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String orderId;

    @Enumerated(EnumType.STRING)
    OtusSystem confirmator;

    @Enumerated(EnumType.STRING)
    OrderRequestType orderRequestType;

    @Enumerated(EnumType.STRING)
    OrderConfirmStatus confirmStatus;

    String errorMessage;

    @OneToMany
    @JoinColumn(name = "order_id")
    List<OrderConfirmItem> orderItems;
}
