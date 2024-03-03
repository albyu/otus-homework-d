package ru.boldyrev.otus.model.dto.rest;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.OrderItem;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RestOrderItem {

    private Long id;

    private int quantity;

    private RestProduct product;


    public RestOrderItem(OrderItem orderItem){
        this.id = orderItem.getId();
        this.quantity = orderItem.getQuantity();
        this.product = new RestProduct(orderItem.getProduct());
    }
}
