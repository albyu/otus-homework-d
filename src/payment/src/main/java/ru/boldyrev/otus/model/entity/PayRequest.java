package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.PaymentGoal;
import ru.boldyrev.otus.model.enums.PaymentResult;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NoArgsConstructor
@Data
@Accessors(chain = true)
@Table(name = "pay_requests")
public class PayRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private String orderId;

    @ManyToOne
    private ClientToken clientToken;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentResult paymentResult;

    private String externalPaymentId;

    @Enumerated(EnumType.STRING)
    private PaymentGoal paymentGoal;

    private Timestamp timestamp;

}
