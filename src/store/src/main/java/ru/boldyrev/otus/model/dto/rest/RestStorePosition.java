package ru.boldyrev.otus.model.dto.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.boldyrev.otus.model.entity.Product;
import ru.boldyrev.otus.model.entity.StorePosition;

@Data
@NoArgsConstructor
public class RestStorePosition {
    RestProduct product;
    RestStore store;
    int quantity;
    public RestStorePosition(StorePosition position){
       this.quantity = position.getQuantity();
       this.product = new RestProduct(position.getProduct());
       this.store = new RestStore(position.getStore());
    }
}
