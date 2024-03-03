package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymSerializationErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public PaymSerializationErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("orch_notify_serialize_error_counter")
                .description("Notification serialization error in orchestra")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}