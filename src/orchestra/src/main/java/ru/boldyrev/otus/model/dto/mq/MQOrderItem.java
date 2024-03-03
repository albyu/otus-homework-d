package ru.boldyrev.otus.model.dto.mq;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.entity.OrderItem;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MQOrderItem {

    @JsonProperty("externalId")
    private Long id;

    private int quantity;

    private Long productId;

    private String productName;

    private double productPrice;

    public MQOrderItem(OrderItem orderItem){
        this.id = orderItem.getId();
        this.quantity = orderItem.getQuantity();
        this.productId = orderItem.getProductId();
        this.productName = orderItem.getProductName();
        this.productPrice = orderItem.getProductPrice();
    }
}
