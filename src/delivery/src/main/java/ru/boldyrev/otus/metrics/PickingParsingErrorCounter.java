package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PickingParsingErrorCounter {
    private final Counter counter;

    @Autowired
    public PickingParsingErrorCounter(MeterRegistry registry) {
        this.counter = Counter.builder("deli_picking_parsing_error_counter")
                .description("Unparsed requests for picking in delivery")
                .register(registry);
    }

    public void increment() {
        counter.increment();
    }
}