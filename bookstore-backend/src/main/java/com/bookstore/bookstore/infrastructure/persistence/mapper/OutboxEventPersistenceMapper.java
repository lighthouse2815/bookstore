package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.OutboxEvent;
import com.bookstore.bookstore.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceMapper {
    public OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        if (entity == null) return null;
        return new OutboxEvent(entity.getId(), entity.getAggregateType(), entity.getAggregateId(), entity.getEventType(),
                entity.getPayload(), entity.getDeduplicationKey(), entity.getStatus(), entity.getAttemptCount(),
                entity.getNextAttemptAt(), entity.getLockedAt(), entity.getLockedBy(), entity.getLastError(),
                entity.getCreatedAt(), entity.getProcessedAt(), entity.getUpdatedAt(), entity.getVersion());
    }
    public void copyToEntity(OutboxEvent event, OutboxEventJpaEntity entity) {
        entity.setId(event.getId());
        entity.setAggregateType(event.getAggregateType());
        entity.setAggregateId(event.getAggregateId());
        entity.setEventType(event.getEventType());
        entity.setPayload(event.getPayload());
        entity.setDeduplicationKey(event.getDeduplicationKey());
        entity.setStatus(event.getStatus());
        entity.setAttemptCount(event.getAttemptCount());
        entity.setNextAttemptAt(event.getNextAttemptAt());
        entity.setLockedAt(event.getLockedAt());
        entity.setLockedBy(event.getLockedBy());
        entity.setLastError(event.getLastError());
        entity.setCreatedAt(event.getCreatedAt());
        entity.setProcessedAt(event.getProcessedAt());
        entity.setUpdatedAt(event.getUpdatedAt());
        entity.setVersion(event.getVersion());
    }
}
