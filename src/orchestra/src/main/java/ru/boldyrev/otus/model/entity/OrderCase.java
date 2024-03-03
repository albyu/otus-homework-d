package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderSagaStatus;

import javax.persistence.*;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "ORDER_CASES")
@NoArgsConstructor
public class OrderCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    OrderSagaStatus status;

    int retryCount;

}
