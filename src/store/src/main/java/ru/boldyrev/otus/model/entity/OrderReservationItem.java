package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "order_item_reservations")
public class OrderReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    OrderConfirmRequest orderConfirmRequest;

    @ManyToOne
    Product product;

    @ManyToOne
    Store store;

    int quantity;
}
