package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestArrivalItem {
    Long storeId;
    Long productId;
    int quantity;
}
