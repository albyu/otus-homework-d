package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmParsingErrorCounter {
    private final Counter requestCounter;

    @Autowired
    public ConfirmParsingErrorCounter(MeterRegistry registry) {
        this.requestCounter = Counter.builder("orchestra_psd_parsing_error_counter")
                .description("Payment/store/delivery confirmation request parsing error in orchestra")
                .register(registry);
    }

    public void increment() {
        requestCounter.increment();
    }
}