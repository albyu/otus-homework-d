package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmParsingErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public ConfirmParsingErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("order_confirm_parsing_error_counter")
                .description("Order confirmation request parsing error in order")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}