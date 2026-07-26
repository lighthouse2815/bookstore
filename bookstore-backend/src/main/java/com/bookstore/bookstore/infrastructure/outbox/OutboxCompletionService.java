package com.bookstore.bookstore.infrastructure.outbox;

import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxCompletionService {
    private final IOutboxEventRepository outboxEventRepository;
    private final OutboxProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markSucceeded(UUID id) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(id).orElse(null);
        if (event == null || event.getStatus() != OutboxStatus.PROCESSING) return;
        event.succeed(Instant.now());
        outboxEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markFailed(UUID id, String error) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(id).orElse(null);
        if (event == null || event.getStatus() != OutboxStatus.PROCESSING) return;
        String safeError = error == null ? "handler failed" : error.replaceAll("[\\r\\n]", " ");
        if (safeError.length() > 2000) safeError = safeError.substring(0, 2000);
        event.fail(safeError, properties.maxAttempts(), Instant.now());
        outboxEventRepository.save(event);
    }
}
