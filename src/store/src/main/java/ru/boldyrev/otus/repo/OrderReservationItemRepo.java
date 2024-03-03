package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;
import ru.boldyrev.otus.model.entity.OrderReservationItem;

import java.util.List;

public interface OrderReservationItemRepo extends JpaRepository<OrderReservationItem, Long> {
    List<OrderReservationItem> findByOrderConfirmRequest(OrderConfirmRequest orderConfirmRequest);
}
