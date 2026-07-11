package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOutboxDeliveryRepository;
import com.bookstore.bookstore.infrastructure.persistence.entity.OutboxDeliveryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.repository.OutboxDeliveryJpaRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxDeliveryRepositoryAdapter implements IOutboxDeliveryRepository {
    private final OutboxDeliveryJpaRepository repository;
    @Override public boolean exists(UUID eventId, String consumer) { return repository.existsByEventIdAndConsumer(eventId, consumer); }
    @Override public void save(UUID eventId, String consumer, Instant deliveredAt) {
        OutboxDeliveryJpaEntity entity = new OutboxDeliveryJpaEntity();
        entity.setId(UUID.randomUUID()); entity.setEventId(eventId); entity.setConsumer(consumer); entity.setDeliveredAt(deliveredAt);
        repository.save(entity);
    }
}
