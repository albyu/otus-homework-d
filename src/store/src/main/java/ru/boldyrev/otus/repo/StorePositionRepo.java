package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.boldyrev.otus.model.entity.Product;
import ru.boldyrev.otus.model.entity.Store;
import ru.boldyrev.otus.model.entity.StorePosition;

import java.util.List;

public interface StorePositionRepo extends JpaRepository<StorePosition, Long> {
    @Query("SELECT sp FROM StorePosition sp WHERE sp.store = :store AND sp.product = :product")
    List<StorePosition> findByStoreAndProduct(@Param("store") Store store, @Param("product") Product product);

    @Query("SELECT sp FROM StorePosition sp WHERE sp.product.id = :productId")
    List<StorePosition> findByProductId(@Param("productId") Long productId);

}
