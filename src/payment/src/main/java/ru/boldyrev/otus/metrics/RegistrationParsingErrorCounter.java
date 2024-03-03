package ru.boldyrev.otus.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistrationParsingErrorCounter {
    private final Counter counter;

    @Autowired
    public RegistrationParsingErrorCounter(MeterRegistry registry) {
        this.counter = Counter.builder("payment_register_parsing_error_counter")
                .description("Client registration requests parsing errors in payment")
                .register(registry);
    }

    public void increment() {
        counter.increment();
    }
}