package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.model.dto.mq.MQProduct;
import ru.boldyrev.otus.model.entity.Product;
import ru.boldyrev.otus.model.dto.rest.RestProduct;
import ru.boldyrev.otus.repo.ProductRepo;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductService {
    private final ProductRepo productRepo;

    public RestProduct get(Long productId) throws ConflictException {

        Product product = productRepo.findById(productId).orElseThrow(()-> new ConflictException("Product not found"));
        return new RestProduct(product);
    }

    public RestProduct saveProduct(RestProduct tProduct) {
        Product product;
        if (tProduct.getId() == null) product = new Product(tProduct);
        else {
            product = productRepo.findById(tProduct.getId()).orElse(new Product(tProduct));
            product.setName(tProduct.getName()).setPrice(tProduct.getPrice());
        }

        product = productRepo.save(product);
        return new RestProduct(product);
    }

    public void saveProduct(MQProduct tProduct) {
        Product product;
        if (tProduct.getId() == null) product = new Product().setId(tProduct.getId()).setName(tProduct.getName()).setPrice(tProduct.getPrice());
        else {
            product = productRepo.findById(tProduct.getId()).orElse(new Product().setId(tProduct.getId()).setName(tProduct.getName()).setPrice(tProduct.getPrice()));
            product.setName(tProduct.getName()).setPrice(tProduct.getPrice());
        }
        productRepo.save(product);
    }

    public boolean delete(Long productId) {
        if (productRepo.findById(productId).isPresent()){
            productRepo.deleteById(productId);
            return true;
        } else {
            return false;
        }
    }
}
