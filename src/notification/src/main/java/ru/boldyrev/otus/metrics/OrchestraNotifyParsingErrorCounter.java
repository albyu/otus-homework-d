package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrchestraNotifyParsingErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public OrchestraNotifyParsingErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("notify_orchestra_parsing_error_counter")
                .description("Orchestra notification parsing error in notification")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}