package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeadLetterErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public NotificationDeadLetterErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("notify_dead_letter_error_counter")
                .description("Notification dead letter error counter")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}