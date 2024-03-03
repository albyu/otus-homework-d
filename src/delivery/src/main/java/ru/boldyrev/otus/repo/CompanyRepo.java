package ru.boldyrev.otus.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.Company;

public interface CompanyRepo extends JpaRepository<Company, String> {
}
