package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaitingForFinalizeCounter {
    private final Counter counter;

    @Autowired
    public WaitingForFinalizeCounter(MeterRegistry registry) {
        this.counter = Counter.builder("order_waiting_finalize_counter")
                .description("Orders waiting for finalize in order")
                .register(registry);
    }

    public void increment() {
        counter.increment();
    }
    public void decrement() {
        counter.increment(-1);
    }
}