package com.bookstore.bookstore.infrastructure.outbox;

import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("outbox")
public class OutboxHealthIndicator implements HealthIndicator {
    private final IOutboxEventRepository repository;
    private final OutboxProperties properties;
    public OutboxHealthIndicator(IOutboxEventRepository repository, OutboxProperties properties) { this.repository = repository; this.properties = properties; }
    @Override public Health health() {
        long pending = repository.countByStatus(OutboxStatus.PENDING);
        long failed = repository.countByStatus(OutboxStatus.FAILED);
        long dead = repository.countByStatus(OutboxStatus.DEAD);
        long backlog = pending + failed + dead;
        Health.Builder result = backlog >= properties.backlogWarningThreshold() ? Health.status("DEGRADED") : Health.up();
        return result.withDetail("pending", pending).withDetail("failed", failed).withDetail("dead", dead)
                .withDetail("backlogWarningThreshold", properties.backlogWarningThreshold()).build();
    }
}
