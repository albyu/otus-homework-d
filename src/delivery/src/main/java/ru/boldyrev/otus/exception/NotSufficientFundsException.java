package ru.boldyrev.otus.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotSufficientFundsException extends Throwable {
   private final String message;
}
