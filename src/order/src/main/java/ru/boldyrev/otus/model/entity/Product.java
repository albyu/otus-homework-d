package ru.boldyrev.otus.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.boldyrev.otus.model.dto.rest.RestProduct;

import javax.persistence.*;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "PRODUCT")
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;

    public Product(RestProduct tProduct){
        this.id = tProduct.getId();
        this.name = tProduct.getName();
        this.price = tProduct.getPrice();
    }
}
