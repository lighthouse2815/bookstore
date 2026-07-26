package com.bookstore.bookstore.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_deliveries")
@Getter
@Setter
@NoArgsConstructor
public class OutboxDeliveryJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;
    @Column(nullable = false, length = 100, updatable = false)
    private String consumer;
    @Column(name = "delivered_at", nullable = false, updatable = false)
    private Instant deliveredAt;
}
