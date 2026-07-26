package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.OutboxDeliveryJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxDeliveryJpaRepository extends JpaRepository<OutboxDeliveryJpaEntity, UUID> {
    boolean existsByEventIdAndConsumer(UUID eventId, String consumer);
}
