package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaitingForConfirmCounter {
    private final Counter counter;

    @Autowired
    public WaitingForConfirmCounter(MeterRegistry registry) {
        this.counter = Counter.builder("order_waiting_confirm_counter")
                .description("Orders waiting for confirm in order")
                .register(registry);
    }

    public void increment() {
        counter.increment();
    }
    public void decrement() {
        counter.increment(-1);
    }
}