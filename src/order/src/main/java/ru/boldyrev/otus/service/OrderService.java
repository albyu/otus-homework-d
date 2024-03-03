package ru.boldyrev.otus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.exception.NotAuthorizedException;
import ru.boldyrev.otus.exception.NotFoundException;
import ru.boldyrev.otus.metrics.WaitingForConfirmCounter;
import ru.boldyrev.otus.metrics.WaitingForFinalizeCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.entity.DeliveryDetails;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.entity.OrderItem;
import ru.boldyrev.otus.model.entity.Product;
import ru.boldyrev.otus.model.enums.OrderStatus;
import ru.boldyrev.otus.model.dto.rest.RestOrder;
import ru.boldyrev.otus.model.dto.rest.RestOrderItem;
import ru.boldyrev.otus.repo.OrderRepo;
import ru.boldyrev.otus.repo.ProductRepo;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final OrderSender orderSender;

    private final WaitingForConfirmCounter waitingForConfirmCounter;

    private final WaitingForFinalizeCounter waitingForFinalizeCounter;

    public Order get(String orderId) throws NotFoundException, ConflictException {
        if (orderId == null) {
            throw new ConflictException("Null orderId");
        }
        return orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    }

    /* Размещение нового заказа */
    @Transactional
    public RestOrder place(RestOrder tOrder, String username) throws ConflictException, NotAuthorizedException, NotFoundException {

        /* Проверим наличие orderId */
        if (tOrder.getId() == null) {
            throw new ConflictException("Null orderId");
        }

        /* Поищем существующий заказ */
        Optional<Order> existingOrder = orderRepo.findById(tOrder.getId());

        /* Добавляем новый заказ */
        if (existingOrder.isEmpty()) {
            //Размещаем новый заказ со статусом NEW и версией 0
            Order order = createOrder(tOrder, username);
            order = orderRepo.saveAndFlush(order);

            waitingForFinalizeCounter.increment();

            return new RestOrder(order);
        }

        /* Заказ уже существует, вернем его состояние as is */
        else {
            /* Этот ли пользователь создавал заказ?*/
            if (!existingOrder.get().getClientId().equals(username))
                throw new NotAuthorizedException("User not authorized");

            /* Вернем состояние заказа */
            return new RestOrder(existingOrder.get());
        }
    }


    private Order createOrder(RestOrder tOrder, String username) throws NotFoundException {
        Order order = new Order().setId(tOrder.getId()).setStatus(OrderStatus.NEW).setVersion(0L).setClientId(username);
        if (tOrder.getDelivery() != null) {
            order.setDelivery(new DeliveryDetails(tOrder.getDelivery()));
        }
        order.setOrderItems(new HashSet<>());
        for (RestOrderItem tItem : tOrder.getOrderItems()) {
            OrderItem item = new OrderItem().setQuantity(tItem.getQuantity());
            /*Поиск продукта*/
            if (tItem.getProduct() == null) throw new NotFoundException("Not found: empty product");
            if (tItem.getProduct().getId() == null) throw new NotFoundException("Not found: empty product");
            Product product = productRepo.findById(tItem.getProduct().getId()).orElseThrow(() -> new NotFoundException("Product not found"));
            item.setProduct(product);
            order.getOrderItems().add(item);
        }
        return order;
    }

    /* Изменение существующего заказа */
    @Transactional
    public RestOrder adjust(Order order, RestOrder tOrder) throws ConflictException, NotFoundException {

        /* Проверяем статус заказа */
        if (order.getStatus() == OrderStatus.IN_PROGRESS) {
            throw new ConflictException("Order already in progress");
        } else if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ConflictException("Order already completed");
        } else if (order.getStatus() == OrderStatus.CANCELED) {
            throw new ConflictException("Order already canceled");
        } else if (order.getStatus() == OrderStatus.PAID) {
            throw new ConflictException("Order already paid");
        } else { /* Order in status NEW*/
            /* Корректируем существующий заказ */
            order = updateOrder(order, tOrder);
            order = orderRepo.saveAndFlush(order);
            return new RestOrder(order);
        }
    }

    /*Корректируем существующий заказ*/
    private Order updateOrder(Order order, RestOrder tOrder) throws NotFoundException {

        order.setVersion(tOrder.getVersion()); /* Для контроля оптимистической блокировки*/

        /*Статус*/
        if (order.getStatus() == OrderStatus.NEW && tOrder.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
        }

        /*Delivery*/
        if (tOrder.getDelivery() != null) {
            order.setDelivery(new DeliveryDetails(tOrder.getDelivery()));
        }



        /* Перебираем items tOrder, ищем, что хотим поменять */
        for (RestOrderItem tItem : tOrder.getOrderItems()) {
            Long tItemId = tItem.getId();
            /* Ищем в Order элемент с таким id */

            OrderItem existingItem = null;

            if (tItemId != null) {
                existingItem = order.getOrderItems().stream()
                        .filter(item -> Objects.equals(item.getId(), tItemId))
                        .findFirst().orElse(null);
            }

            if (existingItem != null) {

                /*Есть такой элемент - апдейтим его*/
                existingItem.setQuantity(tItem.getQuantity());

                if (tItem.getProduct() == null) throw new NotFoundException("Not found: empty product");
                if (tItem.getProduct().getId() == null) throw new NotFoundException("Not found: empty product");
                Product product = productRepo.findById(tItem.getProduct().getId()).orElseThrow(() -> new NotFoundException("Product not found"));

                existingItem.setProduct(product);
            } else {
                /*Нет такого элемента - добавляем новый*/
                OrderItem item = new OrderItem().setQuantity(tItem.getQuantity());

                /*Ищем и устанавливаем продукт*/
                if (tItem.getProduct() == null) throw new NotFoundException("Not found: empty product");
                if (tItem.getProduct().getId() == null) throw new NotFoundException("Not found: empty product");
                Product product = productRepo.findById(tItem.getProduct().getId()).orElseThrow(() -> new NotFoundException("Product not found"));
                item.setProduct(product);
                order.getOrderItems().add(item);
            }
        }
        /*Ищем элементы item, которых нет в tOrder */
        Iterator<OrderItem> iterator = order.getOrderItems().iterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            Long itemId = item.getId();
            if (itemId != null) { /*Нет смысла проверять только что добавленные элементы*/
                if (tOrder.getOrderItems().stream().noneMatch(ti -> Objects.equals(ti.getId(), itemId))) {
                    iterator.remove(); // Удаляем элемент из коллекции order.getOrderItems()
                }
            }
        }

        return order;
    }

    /* Передача заказа в обработку */
    @Transactional
    public RestOrder startProcessing(Order existingOrder, RestOrder tOrder) throws ConflictException {

        /* Проверяем статус заказа */
        if (existingOrder.getStatus() == OrderStatus.PAID || existingOrder.getStatus() == OrderStatus.IN_PROGRESS ||
                existingOrder.getStatus() == OrderStatus.COMPLETED || existingOrder.getStatus() == OrderStatus.FAILED) {

            /* Валидация заказа */
            if (existingOrder.getDelivery() == null)
                throw new ConflictException("Для заказа не задана доставка");

            if (existingOrder.getOrderItems().isEmpty())
                throw new ConflictException("В заказ не добавлен ни один товар");


            /* Меняем статус и сохраняем */
            existingOrder.setStatus(OrderStatus.IN_PROGRESS);
            existingOrder.setVersion(tOrder.getVersion());/* Для контроля оптимистической блокировки*/
            existingOrder = orderRepo.saveAndFlush(existingOrder);

            /* Поставим заказ во внутреннюю очередь */
            try {
                MQOrder transportableOrder = new MQOrder(existingOrder);
                orderSender.sendMessageReq(transportableOrder);

                waitingForConfirmCounter.increment();
                waitingForFinalizeCounter.decrement();

            } catch (JsonProcessingException e) {
                throw new ConflictException("Невозможно обработать заказ");
            }


        } else if (existingOrder.getStatus() == OrderStatus.CANCELED) {
            throw new ConflictException("Already canceled");
        }  else if (existingOrder.getStatus() == OrderStatus.NEW) {
            throw new ConflictException("Order is not paid yet");
        }

        /* и вернем его состояние*/
        return new RestOrder(existingOrder);

    }

    private double calculateAmount(Order order) {
        double amount = 0;
        for (OrderItem orderItem : order.getOrderItems()) {
            amount += orderItem.getQuantity() * orderItem.getProduct().getPrice();
        }
        return amount;
    }

    /* Отмена заказа*/
    @Transactional
    public RestOrder cancel(Order canceledOrder, RestOrder tOrder) throws ConflictException, NotFoundException {

        /* Проверяем статус заказа */
        if (canceledOrder.getStatus() == OrderStatus.COMPLETED) {
            throw new ConflictException("Already completed");

        } else if (canceledOrder.getStatus() == OrderStatus.IN_PROGRESS) {
            throw new ConflictException("Already in progress");

        } else if (canceledOrder.getStatus() == OrderStatus.PAID) {
            throw new ConflictException("Already paid");

        }
        else if (canceledOrder.getStatus() == OrderStatus.NEW) {

            /* Меняем статус и сохраняем */
            canceledOrder.setStatus(OrderStatus.CANCELED);
            canceledOrder.setVersion(tOrder.getVersion()); /* Для контроля оптимистической блокировки*/
            canceledOrder = orderRepo.saveAndFlush(canceledOrder);

            waitingForFinalizeCounter.decrement();
            return new RestOrder(canceledOrder);

        } else /*OrderStatus.CANCELED, OrderStatus.FAILED*/ {
            /* вернем состояние as is */
            return new RestOrder(canceledOrder);
        }
    }


    @Transactional
    public void saveOrder(Order order) {
        orderRepo.saveAndFlush(order);
    }


}
