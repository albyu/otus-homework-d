package ru.boldyrev.otus.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.ConfirmParsingErrorCounter;
import ru.boldyrev.otus.metrics.ConfirmRejectCounter;
import ru.boldyrev.otus.metrics.ProductParsingErrorCounter;
import ru.boldyrev.otus.metrics.WaitingForConfirmCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.dto.mq.MQProduct;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.enums.OrderStatus;
import ru.boldyrev.otus.service.OrderService;
import ru.boldyrev.otus.service.ProductService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class OrderListener {

    private final ObjectMapper objectMapper;

    private final OrderService orderService;

    private final ProductService productService;

    private final ConfirmParsingErrorCounter confirmParsingErrorCounter;

    private final ConfirmRejectCounter confirmRejectCounter;

    private final ProductParsingErrorCounter productParsingErrorCounter;

    private final WaitingForConfirmCounter waitingForConfirmCounter;


    /*Получение итога обработки заказа*/
    @RabbitListener(queues = "${application.rabbitmq.orderres.queueName}")
    public void receiveMessage(String orderAsString) {
        MQOrder mqOrder = new MQOrder();

        try {
            mqOrder = objectMapper.readValue(orderAsString, MQOrder.class);
            log.info("Received processed order: {}, result {}", mqOrder.getId(), mqOrder.getStatus());

            waitingForConfirmCounter.decrement();

            if (mqOrder.getStatus() == OrderStatus.FAILED) {
                confirmRejectCounter.increment();
            }

            /* Ищем заказ по ID */
            Order order = orderService.get(mqOrder.getId());
            if (order.getStatus() == OrderStatus.IN_PROGRESS) {
                order.setStatus(mqOrder.getStatus());
                orderService.saveOrder(order);
            }

        } catch (Exception e) {
            log.error("Cannot process received processed order {}", orderAsString, e);
            System.out.println("Cannot process received processed order: " + orderAsString);
            confirmParsingErrorCounter.increment();
        }
    }

    /*Обновление справочника продуктов*/
    @RabbitListener(queues = "${application.rabbitmq.orderentity.queueName}")
    public void receiveProduct(String productAsString) {
        MQProduct mqProduct = new MQProduct();

        try {
            mqProduct = objectMapper.readValue(productAsString, MQProduct.class);
            log.info("Received product, id {}", mqProduct.getId());

            /* Ищем продукт по ID */
            productService.saveProduct(mqProduct);

        } catch (Exception e) {
            log.error("Cannot process received product {}", productAsString, e);
            productParsingErrorCounter.increment();
        }
    }

}
