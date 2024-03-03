package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.TokenType;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "tokens")
public class ClientToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /* Тип токена */
    @Enumerated(EnumType.STRING)
    TokenType tokenType;

    /* Внешний идентификатор */
    String externalTokenId;

    /* Клиент, которому принадлежит токен */
    @ManyToOne
    Client client;

    /*Счет (если тип WALLET)*/
    @OneToOne
    Account account;

    String description;
}
