package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.Account;
import ru.boldyrev.otus.model.entity.Client;
import ru.boldyrev.otus.model.entity.ClientToken;
import ru.boldyrev.otus.model.enums.TokenType;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RestClientToken {
    Long id;

    /* Тип токена */
    TokenType tokenType;

    /* Внешний идентификатор */
    String externalTokenId;

    /* Клиент, которому принадлежит токен */
    String clientId;

    /* Баланс (если тип WALLET)*/
    Double accountBalance;

    String description;

    public RestClientToken(ClientToken token){
        this.id = token.getId();
        this.tokenType = token.getTokenType();
        this.externalTokenId = token.getExternalTokenId();
        this.clientId = token.getClient().getClientId();
        this.description = token.getDescription();

        if (token.getTokenType() == TokenType.WALLET && token.getAccount()!=null) {
            this.accountBalance = token.getAccount().getBalance();
        }
    }
}
