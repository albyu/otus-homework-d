package ru.boldyrev.otus.model.dto.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
/*Данные, необходимые для закрытия кейса доставки*/
public class RestOrderDeliveryCommand {
    String orderId; /* Номер заказа */

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    LocalDate deliveryDate; /*Дата доставки*/

    Integer hourFrom; /* Для доставки на дом: интервал доставки, час от */
    Integer hourTo; /* Для доставки на дом: интервал доставки, час до  */
}
