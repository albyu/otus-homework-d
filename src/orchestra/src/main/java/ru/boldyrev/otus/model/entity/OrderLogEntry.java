package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
@NoArgsConstructor
@Table(name = "ORDER_LOG")
public class OrderLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String message;

    private Timestamp timestamp;

    public OrderLogEntry(Order order, String message){
        this.order = order;
        this.message = message;
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
    }
}
