package com.bookstore.bookstore.infrastructure.observability;

import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMetrics implements MeterBinder {
    private final IOutboxEventRepository repository;
    @Override
    public void bindTo(MeterRegistry registry) {
        for (OutboxStatus status : OutboxStatus.values()) {
            Gauge.builder("bookstore.outbox.events", repository, source -> source.countByStatus(status))
                    .tag("status", status.name().toLowerCase())
                    .description("Transactional outbox events by status")
                    .register(registry);
        }
    }
}
