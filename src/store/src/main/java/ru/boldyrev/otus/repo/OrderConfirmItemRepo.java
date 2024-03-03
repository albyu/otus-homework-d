package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.OrderConfirmItem;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;

public interface OrderConfirmItemRepo extends JpaRepository<OrderConfirmItem, Long> {
}
