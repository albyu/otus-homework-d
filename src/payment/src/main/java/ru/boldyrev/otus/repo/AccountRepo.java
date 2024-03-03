package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.Account;

public interface AccountRepo extends JpaRepository<Account, Long> {

}
