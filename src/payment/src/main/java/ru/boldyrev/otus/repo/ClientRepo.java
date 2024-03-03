package ru.boldyrev.otus.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldyrev.otus.model.entity.Client;

public interface ClientRepo extends JpaRepository<Client, String> {

}
