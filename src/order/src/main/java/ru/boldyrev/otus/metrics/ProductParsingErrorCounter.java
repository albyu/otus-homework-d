package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductParsingErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public ProductParsingErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("order_product_parsing_error_counter")
                .description("Order product update parsing error in order")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}