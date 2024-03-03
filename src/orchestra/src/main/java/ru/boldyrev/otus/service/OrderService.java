package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldyrev.otus.exception.NotFoundException;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.entity.OrderLogEntry;
import ru.boldyrev.otus.model.dto.rest.RestLogEntry;
import ru.boldyrev.otus.repo.OrderLogEntryRepo;
import ru.boldyrev.otus.repo.OrderRepo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderService {
    private final OrderLogEntryRepo logEntryRepo;
    private final OrderRepo orderRepo;

    public Order getOrderById(String orderId) throws NotFoundException {
        return orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    public void saveOrderAddLogEntry(Order order, String message) {
        OrderLogEntry entry = new OrderLogEntry(order, message);
        logEntryRepo.saveAndFlush(entry);
        orderRepo.saveAndFlush(order);
    }

    @Transactional/* Обработка новой заявки на обработку*/
    public Order registerOrder(MQOrder mqOrder) {
        /*Сперва проверим, есть ли уже такой заказ*/
        Optional<Order> optionalOrder = orderRepo.findById(mqOrder.getId());

        if (optionalOrder.isEmpty()) {

            /* Заказ еще не обрабатывался, создадим кейс здесь*/
            Order order = new Order(mqOrder);
            orderRepo.saveAndFlush(order);

            return order;
        } else {

            return optionalOrder.get();
        }
    }


    public List<RestLogEntry> getOrderLogsByOrderId(String orderId) throws NotFoundException {
        Optional<Order> optionalOrder = orderRepo.findById(orderId);

            if (optionalOrder.isPresent()) {
                List<OrderLogEntry> entries = logEntryRepo.findByOrderId(orderId);
                List<RestLogEntry> transportableEntries = entries.stream()
                        .sorted(Comparator.comparing(OrderLogEntry::getId))
                        .map(RestLogEntry::new)
                        .toList();

                return transportableEntries;
            }

        throw new NotFoundException("Order not found");

    }
}
