package ru.boldyrev.otus.model.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    PAYMENT_RESULT("Payment result"),
    ORDER_CONFIRMATION("Order confirmation"),
    DELIVERY_NOTIFICATION("Delivery notification");
    private final String name;

    NotificationType(String name) {
        this.name = name;
    }


}
