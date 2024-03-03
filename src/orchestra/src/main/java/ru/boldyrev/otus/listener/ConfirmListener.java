package ru.boldyrev.otus.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.boldyrev.otus.metrics.ConfirmParsingErrorCounter;
import ru.boldyrev.otus.metrics.ConfirmSerializationErrorCounter;
import ru.boldyrev.otus.model.dto.mq.MQOrder;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmationAdvice;
import ru.boldyrev.otus.model.entity.Order;
import ru.boldyrev.otus.model.enums.*;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmResponse;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.service.NotificationService;
import ru.boldyrev.otus.service.OrderService;

@Slf4j
@Component
public class ConfirmListener {

    private static final int MAX_TRY_COUNT = 3;

    @Autowired
    @Lazy
    public ConfirmListener(RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper,
                           OrderService orderService,
                           NotificationService notificationService,
                           OrderListener orderListener, ConfirmParsingErrorCounter confirmParsingErrorCounter, ConfirmSerializationErrorCounter confirmSerializationErrorCounter) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.orderListener = orderListener;
        this.confirmParsingErrorCounter = confirmParsingErrorCounter;
        this.confirmSerializationErrorCounter = confirmSerializationErrorCounter;
    }

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final OrderListener orderListener;
    private final ConfirmParsingErrorCounter confirmParsingErrorCounter;
    private final ConfirmSerializationErrorCounter confirmSerializationErrorCounter;

    @Value("${application.rabbitmq.confirmreq.exchangeName}")
    private String reqConfirmExchange;
    public static final String PAYMENT_REQ_ROUTEKEY = "payment";
    public static final String STORE_REQ_ROUTEKEY = "store";
    public static final String DELIVERY_REQ_ROUTEKEY = "delivery";




    public void sendOrderToConfirm(MQOrderConfirmRequest order, String routeKey) {
        try {
            String caseOrderAsString = null;
            caseOrderAsString = objectMapper.writeValueAsString(order);
            rabbitTemplate.convertAndSend(reqConfirmExchange, routeKey, caseOrderAsString);
        } catch (Exception e) {
            log.error("Cannot send confirm request for order " + order.getTransportableOrder().getId(), e);
            confirmSerializationErrorCounter.increment();
        }
    }

    @RabbitListener(queues = "${application.rabbitmq.confirmres.queueName}")
    public void getConfirmRes(String confirmAsString) {

        MQOrderConfirmResponse confirmResponse;
        try {
            confirmResponse = objectMapper.readValue(confirmAsString, MQOrderConfirmResponse.class);

            /* Найдем order */
            if (confirmResponse == null) {
                log.error("Error in confirm processing");
                return;
            }

            Order order = orderService.getOrderById(confirmResponse.getOrderId());
            //Order order = orderCase.getOrder();

            log.info("Received confirm response for orderId = {} from {}, result {}", order.getId(), confirmResponse.getConfirmator().getName(), confirmResponse.getOrderConfirmStatus().getName());

            /*разбираем от какой системы пришло*/
            /*От payments*/
            if (confirmResponse.getConfirmator() == OtusSystem.PAYMENT) {
                if (confirmResponse.getRequestType() == OrderRequestType.CONFIRM) {
                    /*Разбираем результат выполнения в payment*/
                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        /* Платеж подтвержден, сохраняем статус и отдаем запрос на подтверждение на склад */

                        order.setSagaStatus(OrderSagaStatus.PAYMENT_CONFIRMED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order confirmed by payment, ref_id = " + confirmResponse.getReferenceId()  + ", sent confirm request to store");

                        /* отправить запрос конферма в store */
                        MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                OrderRequestType.CONFIRM,
                                OtusSystem.STORE);
                        sendOrderToConfirm(request, STORE_REQ_ROUTEKEY);

                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        /* Платеж не прошел, сохраняем статус и отдаем ответ FAILED системе заказов */

                        order.setStatus(OrderStatus.FAILED);
                        order.setSagaStatus(OrderSagaStatus.FAILED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order rejected by payment, ref_id = " + confirmResponse.getReferenceId() + ", marked as FAILED, sent back to order");

                        /* Отправить результат в orders */
                        orderListener.sendOrderRes(new MQOrder(order));

                        /* И уведомление в notification*/
                        notificationService.sendNotification(new MQOrderConfirmationAdvice(order));


                    }
                } else if (confirmResponse.getRequestType() == OrderRequestType.REVERSE) {
                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        /* Отмена платежа прошла удачно, теперь можно сохранить статус и отдать ответ FAILED системе заказов */

                        order.setStatus(OrderStatus.FAILED);
                        order.setSagaStatus(OrderSagaStatus.FAILED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order reversed by payment, ref_id = " + confirmResponse.getReferenceId() + ", marked as FAILED, sent back to order");

                        /* Отправить результат в orders */
                        orderListener.sendOrderRes(new MQOrder(order));

                        /* И уведомление в notification*/
                        notificationService.sendNotification(new MQOrderConfirmationAdvice(order));


                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        /* Отмена платежа отвергнута, еще раз попробовать отменить платеж в payments, инкрементировать try_count */
                        /* оставляем статус OrderCaseStatus.STORE_FAILED */
                        int retryCount = order.getRetryCount();
                        if (retryCount < MAX_TRY_COUNT) {
                            order.setSagaStatus(OrderSagaStatus.STORE_FAILED).setRetryCount(retryCount + 1);
                            orderService.saveOrderAddLogEntry(order, "Order reversal rejected by payment (retryCount = "+ String.valueOf(retryCount) +"), ref_id = " + confirmResponse.getReferenceId() + ", retried");
                            MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                    OrderRequestType.REVERSE,
                                    OtusSystem.PAYMENT);
                            sendOrderToConfirm(request, PAYMENT_REQ_ROUTEKEY);

                        } else {
                            order.setSagaStatus(OrderSagaStatus.STORE_FAILED).setRetryCount(retryCount + 1);
                            orderService.saveOrderAddLogEntry(order, "Order reversal rejected by payment (retryCount = "+ String.valueOf(retryCount) +"), ref_id = " + confirmResponse.getReferenceId() + ", retry limit reached");
                        }
                    }
                }

            } /*Разбираем результат выполнения в store*/ else if (confirmResponse.getConfirmator() == OtusSystem.STORE) {
                if (confirmResponse.getRequestType() == OrderRequestType.CONFIRM) {
                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        /*store подтвердил заказ, сохраняем статус и продвигаем его дальше в delivery*/

                        order.setSagaStatus(OrderSagaStatus.STORE_CONFIRMED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order confirmed by store, ref_id = " + confirmResponse.getReferenceId() + ", sent confirm request to delivery");

                        /* Отправить запрос конферма в delivery */
                        MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                OrderRequestType.CONFIRM,
                                OtusSystem.DELIVERY);
                        sendOrderToConfirm(request, DELIVERY_REQ_ROUTEKEY);

                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        /*store отверг запрос, сохраняем статус и шлем отмену в payments*/

                        order.setSagaStatus(OrderSagaStatus.STORE_FAILED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order rejected by store, ref_id = " + confirmResponse.getReferenceId() + ", sent reversal to payment");
                        /* Отправить запрос на отмену платежа в payments */
                        MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                OrderRequestType.REVERSE,
                                OtusSystem.PAYMENT);
                        sendOrderToConfirm(request, PAYMENT_REQ_ROUTEKEY);

                    }
                } else if (confirmResponse.getRequestType() == OrderRequestType.REVERSE) {
                    /* Удачно выполнена отмена в store - сохраняем статус и отменяем в payment*/

                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        order.setSagaStatus(OrderSagaStatus.STORE_FAILED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order reversed by store, ref_id = " + confirmResponse.getReferenceId() + ", sent reversal to payment");
                        /* Отправить запрос на отмену платежа в payments */
                        MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                OrderRequestType.REVERSE,
                                OtusSystem.PAYMENT);
                        sendOrderToConfirm(request, PAYMENT_REQ_ROUTEKEY);


                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        //orderCase.setStatus(OrderCaseStatus.DELIVERY_FAILED);
                        /*Отмена резервирования store неудачна - еще раз попробовать отменить резервирование в store,
                         * инкрементировать try_count */
                        int retryCount = order.getRetryCount();
                        if (retryCount < MAX_TRY_COUNT) {
                            order.setSagaStatus(OrderSagaStatus.DELIVERY_FAILED).setRetryCount(retryCount + 1);
                            orderService.saveOrderAddLogEntry(order, "Order reversal rejected by store (retryCount = "+ String.valueOf(retryCount) +"), ref_id = " + confirmResponse.getReferenceId() + ", retried");
                            MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                    OrderRequestType.REVERSE,
                                    OtusSystem.STORE);
                            sendOrderToConfirm(request, STORE_REQ_ROUTEKEY);
                        }
                        else{
                            order.setSagaStatus(OrderSagaStatus.DELIVERY_FAILED).setRetryCount(retryCount + 1);
                            orderService.saveOrderAddLogEntry(order, "Order reversal rejected by store (retryCount = "+ String.valueOf(retryCount) +"), ref_id = " + confirmResponse.getReferenceId() + ", retry limit reached");
                        }
                    }
                }
            } /*Разбираем результат выполнения в delivery*/ else if (confirmResponse.getConfirmator() == OtusSystem.DELIVERY) {
                if (confirmResponse.getRequestType() == OrderRequestType.CONFIRM) {
                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        // Доставка подтверждена - заказ оформлен. Изменяем статус заявки и заказа, отправляем его в
                        // orders
                        order.setStatus(OrderStatus.COMPLETED);
                        order.setSagaStatus(OrderSagaStatus.COMPLETED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order confirmed by delivery, ref_id = " + confirmResponse.getReferenceId() + ", marked as COMPLETED, sent back to order");

                        /* Сохранить и отправить результат в orders */
                        orderListener.sendOrderRes(new MQOrder(order));

                        /* И уведомление в notification*/
                        notificationService.sendNotification(new MQOrderConfirmationAdvice(order));

                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        //Подтверждение заявки отвергнуто - сохраняем статус, отменяем резервирование на складе

                        order.setSagaStatus(OrderSagaStatus.DELIVERY_FAILED).setRetryCount(0);
                        orderService.saveOrderAddLogEntry(order, "Order rejected by delivery, ref_id = " + confirmResponse.getReferenceId() + ", sent reversal to store");
                        /* Отправить отмену резервирования в store */
                        MQOrderConfirmRequest request = new MQOrderConfirmRequest(order,
                                OrderRequestType.REVERSE,
                                OtusSystem.STORE);
                        sendOrderToConfirm(request, STORE_REQ_ROUTEKEY);
                    }
                } else if (confirmResponse.getRequestType() == OrderRequestType.REVERSE) {
                    if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.CONFIRMED) {
                        //Отмена доставки подтверждена - сохраняем статус, отменяем store
                        //(невозможный кейс, будет актуален, если за доставкой появится еще одна система

                    } else if (confirmResponse.getOrderConfirmStatus() == OrderConfirmStatus.REJECTED) {
                        /*Отмена резервирования delivery неудачна - еще раз попробовать отменить резервирование в delivery,
                         * инкрементировать try_count */
                        //(невозможный кейс, будет актуален, если за доставкой появится еще одна система)
                    }
                }
            }


        } catch (Exception e) {
            log.error("Error in confirm processing", e);
            confirmParsingErrorCounter.increment();
        }

    }


}
