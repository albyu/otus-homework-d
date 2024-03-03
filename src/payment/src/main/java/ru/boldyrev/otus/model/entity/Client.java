package ru.boldyrev.otus.model.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "clients")
public class Client {
    @Id
    String clientId;
}
