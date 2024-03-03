package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.Store;


public interface StoreRepo extends JpaRepository<Store, Long> {
}
