package ru.boldyrev.otus.model.dto.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.Product;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQProduct {
    private Long id;
    private String name;
    private double price;

    public MQProduct(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
    }

}
