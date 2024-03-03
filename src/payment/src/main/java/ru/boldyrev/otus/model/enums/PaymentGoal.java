package ru.boldyrev.otus.model.enums;


import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentGoal {

    ORDER("Order"),
    WALLET_CREDIT("Wallet Credit");

    private final String name;

    PaymentGoal(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}

