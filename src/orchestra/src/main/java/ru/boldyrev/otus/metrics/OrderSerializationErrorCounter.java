package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSerializationErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public OrderSerializationErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("orch_order_serialize_error_counter")
                .description("Order response serialization error in orchestra")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}