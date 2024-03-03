package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;

import java.util.List;

public interface OrderConfirmRequestRepo  extends JpaRepository<OrderConfirmRequest, Long> {
    List<OrderConfirmRequest> findByOrderId(String orderId);
    List<OrderConfirmRequest> findByOrderIdAndConfirmStatus(String orderId, OrderConfirmStatus orderConfirmStatus);
}
