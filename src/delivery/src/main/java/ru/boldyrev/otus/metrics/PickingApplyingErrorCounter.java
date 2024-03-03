package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PickingApplyingErrorCounter {
    private final Counter counter;

    @Autowired
    public PickingApplyingErrorCounter(MeterRegistry registry) {
        this.counter = Counter.builder("deli_picking_applying_error_counter")
                .description("Orders not found for picking in delivery")
                .register(registry);
    }

    public void increment() {
        counter.increment();
    }
}