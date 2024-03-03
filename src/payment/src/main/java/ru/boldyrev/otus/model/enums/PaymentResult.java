package ru.boldyrev.otus.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentResult {
    SUCCESS("Success"),
    NOT_SUFFICIENT_FUNDS("Not sufficient funds"),
    NO_ACCOUNT("No account"),
    EXTERNAL_DECLINE("External decline"),
    IN_PROGRESS ("In progress"),
    REVERSED("Reversed");

    private final String name;

    PaymentResult(String name) {
        this.name = name;
    }
    @JsonValue
    public String getName() {
        return name;
    }
}
