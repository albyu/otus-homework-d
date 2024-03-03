package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.boldyrev.otus.model.entity.PickupPoint;

import java.util.List;

public interface PickupPointRepo extends JpaRepository<PickupPoint, Long> {
    @Query(value = "SELECT * FROM pickup_points WHERE address LIKE :regex", nativeQuery = true)
    List<PickupPoint> findAllByAddressLike(@Param("regex") String like);
}
