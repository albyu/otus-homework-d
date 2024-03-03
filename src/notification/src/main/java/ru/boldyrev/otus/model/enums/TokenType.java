package ru.boldyrev.otus.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TokenType {
    BANK_CARD("Bank card"),
    WALLET("Wallet");

    private final String name;

    TokenType(String name) {
        this.name = name;
    }
    @JsonValue
    public String getName() {
        return name;
    }
}
