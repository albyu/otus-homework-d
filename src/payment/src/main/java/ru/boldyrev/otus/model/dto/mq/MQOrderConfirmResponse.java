package ru.boldyrev.otus.model.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.model.enums.OtusSystem;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MQOrderConfirmResponse {

    String orderId;

    /* Тип запроса */
    OrderRequestType requestType;

    /*Имя системы, подвердившей или отвергшей заказ */
    OtusSystem confirmator;

    /* Результат подтверждения */
    OrderConfirmStatus orderConfirmStatus;

    /* Текст ошибки */
    String errorMessage;

    /* Ссылка на Id в системе, подтвердившей заказ */
    String referenceId;
}
