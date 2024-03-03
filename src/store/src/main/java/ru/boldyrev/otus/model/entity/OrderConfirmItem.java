package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.dto.mq.MQOrderItem;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "order_confirm_items")
public class OrderConfirmItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderConfirmRequest orderRequest;
    private Long productId;
    private int quantity;


}
