package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.model.dto.mq.MQProduct;
import ru.boldyrev.otus.model.dto.rest.*;
import ru.boldyrev.otus.model.entity.Product;
import ru.boldyrev.otus.model.entity.Store;
import ru.boldyrev.otus.model.entity.StorePosition;
import ru.boldyrev.otus.repo.ProductRepo;
import ru.boldyrev.otus.repo.StorePositionRepo;
import ru.boldyrev.otus.repo.StoreRepo;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoreService {

    private final StoreRepo storeRepo;
    private final ProductRepo productRepo;
    private final StorePositionRepo storePositionRepo;
    private final NotificationService notificationService;


    @Transactional
    public void arrive(List<RestArrivalItem> restArrival) throws ConflictException {
        /*По всем элементам прихода на склад*/
        for (RestArrivalItem arrivalItem : restArrival) {
            /* Ищем продукт */
            Optional<Product> product = productRepo.findById(arrivalItem.getProductId());
            if (product.isEmpty())
                throw new ConflictException("Product id " + arrivalItem.getProductId() + " not found");

            /* Ищем склад */
            Optional<Store> store = storeRepo.findById(arrivalItem.getStoreId());
            if (store.isEmpty()) throw new ConflictException("Store id " + arrivalItem.getStoreId() + " not found");

            /* Ищем товарную позицию */
            List<StorePosition> storePositionList = storePositionRepo.findByStoreAndProduct(store.get(), product.get());
            StorePosition storePosition;
            if (storePositionList.isEmpty()) {
                storePosition = new StorePosition().setStore(store.get()).setProduct(product.get()).setQuantity(arrivalItem.getQuantity());
            } else {
                /* Увеличиваем количество товара на складе */
                storePosition = storePositionList.get(0);
                storePosition.setQuantity(storePosition.getQuantity() + arrivalItem.getQuantity());
            }
            /* Сохраняем */
            storePositionRepo.saveAndFlush(storePosition);
        }
    }

    @Transactional
    public RestStore storeAdjust(RestStore restStore) {
        Store store = null;
        if (restStore.getId() != null) {
            store = storeRepo.findById(restStore.getId()).orElse(null);
        }
        if (store == null) {
            store = new Store().setName(restStore.getName()).setAddress(restStore.getAddress()).setId(restStore.getId());
        } else {
            store.setAddress(restStore.getAddress()).setName(restStore.getName());
        }
        store = storeRepo.saveAndFlush(store);
        return new RestStore(store);
    }

    @Transactional
    public RestProduct productAdjust(RestProduct restProduct) {
        Product product = null;
        if (restProduct.getId() != null) {
            product = productRepo.findById(restProduct.getId()).orElse(null);
        }
        if (product == null) {
            product = new Product().setName(restProduct.getName()).setPrice(restProduct.getPrice()).setId(restProduct.getId());
        } else {
            product.setName(restProduct.getName()).setPrice(restProduct.getPrice());
        }
        product = productRepo.saveAndFlush(product);

        notificationService.sendProductNotification(new MQProduct(product));

        return new RestProduct(product);
    }


    public List<RestStorePosition> getStorePositions(Long productId) {
      return storePositionRepo.findByProductId(productId).stream().map(RestStorePosition::new).toList();
    }


}
