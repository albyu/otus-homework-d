package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.TokenType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQClientToken {
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

}
