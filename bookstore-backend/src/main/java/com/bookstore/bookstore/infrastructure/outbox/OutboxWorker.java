package com.bookstore.bookstore.infrastructure.outbox;

import com.bookstore.bookstore.domain.model.OutboxEvent;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutboxProperties properties;
    private final OutboxClaimService outboxClaimService;
    private final OutboxEventDeliveryService outboxEventDeliveryService;
    private final OutboxCompletionService outboxCompletionService;
    private final String workerId = resolveWorkerId();

    @Scheduled(fixedDelayString = "${app.outbox.delay-ms:5000}")
    public void process() {
        if (!properties.enabled()) return;
        Instant now = Instant.now();
        int reclaimed = outboxClaimService.reclaimStale(properties.batchSize(), now.minusSeconds(properties.processingTimeoutSeconds()), now);
        if (reclaimed > 0) log.warn("Reclaimed {} stale transactional outbox events", reclaimed);
        List<OutboxEvent> events = outboxClaimService.claim(properties.batchSize(), workerId, now);
        for (OutboxEvent event : events) {
            try {
                outboxEventDeliveryService.deliver(event);
                outboxCompletionService.markSucceeded(event.getId());
            } catch (RuntimeException exception) {
                log.warn("Transactional outbox delivery failed eventId={} eventType={}", event.getId(), event.getEventType(), exception);
                outboxCompletionService.markFailed(event.getId(), exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        }
    }

    private static String resolveWorkerId() {
        try { return InetAddress.getLocalHost().getHostName() + ":" + ProcessHandle.current().pid(); }
        catch (Exception ignored) { return "bookstore:" + ProcessHandle.current().pid(); }
    }
}
