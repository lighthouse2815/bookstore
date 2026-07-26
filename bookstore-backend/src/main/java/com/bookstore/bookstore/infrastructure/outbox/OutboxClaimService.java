package com.bookstore.bookstore.infrastructure.outbox;

import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxClaimService {
    private final IOutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public List<OutboxEvent> claim(int batchSize, String workerId, Instant now) {
        List<OutboxEvent> events = outboxEventRepository.findClaimableForUpdate(now, batchSize);
        return events.stream().map(event -> {
            event.claim(workerId, now);
            return outboxEventRepository.save(event);
        }).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int reclaimStale(int batchSize, Instant before, Instant now) {
        List<OutboxEvent> events = outboxEventRepository.findStaleProcessingForUpdate(before, batchSize);
        events.forEach(event -> {
            event.reclaim(now);
            outboxEventRepository.save(event);
        });
        return events.size();
    }
}
