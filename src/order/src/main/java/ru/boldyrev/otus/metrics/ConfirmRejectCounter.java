package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmRejectCounter {
    private final Counter requestCounter;

    @Autowired
    public ConfirmRejectCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("order_confirm_reject_counter")
                .description("Order confirmation rejects in order")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}