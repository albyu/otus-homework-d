package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifySerializationErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public NotifySerializationErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("payment_notify_serialize_error_counter")
                .description("Notification serialization error in payment")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}