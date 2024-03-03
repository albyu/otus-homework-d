package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.boldyrev.otus.model.entity.OrderLogEntry;

import java.util.List;

public interface OrderLogEntryRepo extends JpaRepository<OrderLogEntry, Long> {
    @Query("SELECT o FROM OrderLogEntry o WHERE o.order.id = :orderId")
    List<OrderLogEntry> findByOrderId(@Param("orderId")String orderId);
}
