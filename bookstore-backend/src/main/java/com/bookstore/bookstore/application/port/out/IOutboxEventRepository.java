package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IOutboxEventRepository {
    Optional<OutboxEvent> findById(UUID id);
    Optional<OutboxEvent> findByIdForUpdate(UUID id);
    Optional<OutboxEvent> findByDeduplicationKey(String key);
    List<OutboxEvent> findClaimableForUpdate(Instant now, int limit);
    List<OutboxEvent> findStaleProcessingForUpdate(Instant before, int limit);
    PageSliceResult<OutboxEvent> findPage(int page, int size, OutboxStatus status);
    long countByStatus(OutboxStatus status);
    OutboxEvent save(OutboxEvent event);
}
