package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmSerializationErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public ConfirmSerializationErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("orch_confirm_serialize_error_counter")
                .description("Confirm request serialization error in orchestra")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}