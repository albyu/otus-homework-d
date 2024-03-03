package ru.boldyrev.otus.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum OrderConfirmStatus {
    CONFIRMED("Confirmed"),
    REJECTED("Rejected"),
    IN_PROGRESS("In progress");

    private final String name;

    OrderConfirmStatus(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
