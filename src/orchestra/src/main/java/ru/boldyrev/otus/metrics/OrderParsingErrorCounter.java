package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderParsingErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public OrderParsingErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("orchestra_order_parsing_error_counter")
                .description("Order confirm parsing error in orchestra")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}