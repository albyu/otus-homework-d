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
        this.requestCounter = Counter.builder("deli_confirm_parsing_error_counter")
                .description("Confirmation request parsing error in delivery")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}