package ru.boldyrev.otus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldyrev.otus.exception.ConflictException;
import ru.boldyrev.otus.metrics.ConfirmRejectCounter;
import ru.boldyrev.otus.model.dto.detached.DetachedOrderReservationItem;
import ru.boldyrev.otus.model.dto.mq.MQOrderConfirmRequest;
import ru.boldyrev.otus.model.dto.mq.MQOrderItem;
import ru.boldyrev.otus.model.dto.mq.MQOrderNotification;
import ru.boldyrev.otus.model.dto.rest.RestOrderNotification;
import ru.boldyrev.otus.model.entity.OrderConfirmItem;
import ru.boldyrev.otus.model.entity.OrderConfirmRequest;
import ru.boldyrev.otus.model.entity.OrderReservationItem;
import ru.boldyrev.otus.model.entity.StorePosition;
import ru.boldyrev.otus.model.enums.OrderConfirmStatus;
import ru.boldyrev.otus.model.enums.OrderRequestType;
import ru.boldyrev.otus.repo.OrderConfirmItemRepo;
import ru.boldyrev.otus.repo.OrderConfirmRequestRepo;
import ru.boldyrev.otus.repo.OrderReservationItemRepo;
import ru.boldyrev.otus.repo.StorePositionRepo;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfirmService {

    private final StorePositionRepo storePositionRepo;

    private final OrderReservationItemRepo orderReservationItemRepo;

    private final OrderConfirmItemRepo orderConfirmItemRepo;

    private final OrderConfirmRequestRepo orderConfirmRequestRepo;

    private final NotificationService notificationService;

    private final ConfirmRejectCounter confirmRejectCounter;


    @Transactional
    public OrderConfirmRequest register(MQOrderConfirmRequest mqRequest) {
        OrderConfirmRequest confirmRequest = null;

        if (mqRequest.getOrderRequestType() == OrderRequestType.CONFIRM) {

            /* Поищем уже обработанный confirm */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(mqRequest.getTransportableOrder().getId(), OrderConfirmStatus.CONFIRMED);

            /* Если уже есть, возвращаем его */
            if (!requests.isEmpty())
                return requests.get(0);

        } else if (mqRequest.getOrderRequestType() == OrderRequestType.REVERSE) {

            /* Поищем уже сверсированный confirm */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(mqRequest.getTransportableOrder().getId(), OrderConfirmStatus.REVERSED);

            /* Если уже есть, возвращаем его */
            if (!requests.isEmpty())
                return requests.get(0);

        }

        /* Иначе сохраняем request и возвращаем его */
        confirmRequest = new OrderConfirmRequest()
                .setOrderId(mqRequest.getTransportableOrder().getId())
                .setConfirmator(mqRequest.getConfirmator())
                .setOrderRequestType(mqRequest.getOrderRequestType())
                .setConfirmStatus(OrderConfirmStatus.IN_PROGRESS);

        List<OrderConfirmItem> items = new ArrayList<>();
        for (MQOrderItem mqOrderItem : mqRequest.getTransportableOrder().getOrderItems()) {
            OrderConfirmItem item = new OrderConfirmItem()
                    .setProductId(mqOrderItem.getProductId())
                    .setQuantity(mqOrderItem.getQuantity());
            items.add(item);
        }
        orderConfirmItemRepo.saveAllAndFlush(items);
        confirmRequest.setOrderItems(items);
        orderConfirmRequestRepo.saveAndFlush(confirmRequest);

        return confirmRequest;

    }


    @Transactional
    public OrderConfirmRequest process(OrderConfirmRequest confirmRequest) {
        /* Создаем резервирования */
        List<OrderReservationItem> reservationItems = new ArrayList<>();
        List<StorePosition> affectedPositions = new ArrayList<>();
        List<StorePosition> deletedPosition = new ArrayList<>();

        List<DetachedOrderReservationItem> detachedOrderReservationItems = new ArrayList<>();


        /* Обрабатываем request */
        if (confirmRequest.getOrderRequestType() == OrderRequestType.CONFIRM) {

            boolean successfulReservation = true;

            for (OrderConfirmItem item : confirmRequest.getOrderItems()) {
                List<StorePosition> positions = storePositionRepo.findByProductId(item.getProductId());
                int quantity = item.getQuantity();

                /* Пытаемся набрать нужное количество*/
                for (StorePosition position : positions) {
                    int reservationQuantity = Math.min(quantity, position.getQuantity());

                    DetachedOrderReservationItem detachedOrderReservationItem =
                            new DetachedOrderReservationItem().setQuantity(reservationQuantity)
                                    .setStorePosition(position)
                                    .setEntireStorePosition(reservationQuantity>=position.getQuantity());

                    quantity -= reservationQuantity;

                    detachedOrderReservationItems.add(detachedOrderReservationItem);

                    if (quantity <= 0) break; /*Уже набрали нужное количество*/
                }

                if (quantity > 0) { /*Не смогли набрать нужное количество*/
                    successfulReservation = false;
                    break;
                }
            }
            if (successfulReservation) { /* Резервирование успешно */

                confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED);

                for (DetachedOrderReservationItem detachedItem: detachedOrderReservationItems){
                    OrderReservationItem orderReservationItem =
                            new OrderReservationItem().setOrderConfirmRequest(confirmRequest)
                                    .setProduct(detachedItem.getStorePosition().getProduct())
                                    .setStore(detachedItem.getStorePosition().getStore())
                                    .setQuantity(detachedItem.getQuantity());

                    reservationItems.add(orderReservationItem);

                    if (detachedItem.getEntireStorePosition()){
                        deletedPosition.add(detachedItem.getStorePosition());
                    } else {
                        detachedItem.getStorePosition().
                                setQuantity(detachedItem.getStorePosition().getQuantity() - detachedItem.getQuantity());
                        affectedPositions.add(detachedItem.getStorePosition());
                    }

                }

                storePositionRepo.deleteAll(deletedPosition);
                storePositionRepo.saveAllAndFlush(affectedPositions);
                orderReservationItemRepo.saveAllAndFlush(reservationItems);
                orderConfirmRequestRepo.saveAndFlush(confirmRequest);
            } else {

                confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED);

                orderConfirmRequestRepo.saveAndFlush(confirmRequest);

                confirmRejectCounter.increment();
            }


        } else if (confirmRequest.getOrderRequestType() == OrderRequestType.REVERSE) {
            /* Находим подтвержденное резервирование */
            List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderIdAndConfirmStatus(confirmRequest.getOrderId(), OrderConfirmStatus.CONFIRMED);

            /* Если подтвержденное резервирование есть */
            if (!requests.isEmpty()) {
                reservationItems = orderReservationItemRepo.findByOrderConfirmRequest(requests.get(0));
                /* Отменяем все ранее зарезервированные позиции */
                for (OrderReservationItem reservationItem : reservationItems) {
                    StorePosition position = rollbackReservation(reservationItem);
                    affectedPositions.add(position);
                }


                /* Помечаем оригинальный запрос на резервирование как REVERSED */
                requests.get(0).setConfirmStatus(OrderConfirmStatus.REVERSED);
                orderConfirmRequestRepo.saveAndFlush(requests.get(0));

                /* Помечаем запрос на резервирование как CONFIRMED */
                confirmRequest.setConfirmStatus(OrderConfirmStatus.CONFIRMED);

                storePositionRepo.saveAllAndFlush(affectedPositions);
                orderReservationItemRepo.saveAllAndFlush(reservationItems);

                orderConfirmRequestRepo.saveAndFlush(confirmRequest);
            }
            /* Если подтвержденного резервирования нет - то и отменять нечего */
            else {
                confirmRequest.setConfirmStatus(OrderConfirmStatus.REJECTED);
                orderConfirmRequestRepo.saveAndFlush(confirmRequest);
                confirmRejectCounter.increment();
            }


        }
        return confirmRequest;
    }

    
    private StorePosition rollbackReservation(OrderReservationItem item) {

        List<StorePosition> storePositionList = storePositionRepo.findByStoreAndProduct(item.getStore(), item.getProduct());
        StorePosition storePosition;
        if (storePositionList.isEmpty()) {
            storePosition = new StorePosition().setQuantity(item.getQuantity()).setStore(item.getStore()).setProduct(item.getProduct());
        } else {
            storePosition = storePositionList.get(0);
            storePosition.setQuantity(storePosition.getQuantity() + item.getQuantity());
        }
        return storePosition;
    }


    @Transactional
    public RestOrderNotification processOrder(RestOrderNotification restOrderNotification) throws ConflictException {
        List<OrderConfirmRequest> requests = orderConfirmRequestRepo.findByOrderId(restOrderNotification.getOrderId());

        /* Ищем заказы в статусе CONFIRMED */
        List<OrderConfirmRequest> confirmedRequests = requests.stream().filter(o -> (o.getConfirmStatus() == OrderConfirmStatus.CONFIRMED)).toList();
        if (!confirmedRequests.isEmpty()) {
            OrderConfirmRequest confirm = confirmedRequests.get(0);
            confirm.setConfirmStatus(OrderConfirmStatus.PROCESSED);
            orderConfirmRequestRepo.saveAndFlush(confirm);


        } else {
            List<OrderConfirmRequest> processedRequests = requests.stream().filter(o -> (o.getConfirmStatus() == OrderConfirmStatus.PROCESSED)).toList();
            if (processedRequests.isEmpty()) {
                throw new ConflictException("Не найден одобренный заказ " + restOrderNotification.getOrderId());
            } /*Иначе заказ уже одобрен, идемпотентность*/

        }
        notificationService.sendOrderNotification(new MQOrderNotification().setOrderId(restOrderNotification.getOrderId()).setTimestamp(Timestamp.valueOf(LocalDateTime.now())));
        return restOrderNotification.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
    }
}
