package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductNotifySerializationErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public ProductNotifySerializationErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("store_product_notify_serialize_error_counter")
                .description("Product notification serialization error in store")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}