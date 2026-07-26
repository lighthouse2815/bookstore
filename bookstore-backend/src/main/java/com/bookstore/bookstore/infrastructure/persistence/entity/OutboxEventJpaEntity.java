package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_claimable", columnList = "status,next_attempt_at,created_at"),
        @Index(name = "idx_outbox_processing_lock", columnList = "status,locked_at")
})
@Getter
@Setter
@NoArgsConstructor
public class OutboxEventJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(name = "aggregate_type", nullable = false, length = 80, updatable = false)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 100, updatable = false)
    private String eventType;
    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String payload;
    @Column(name = "deduplication_key", nullable = false, length = 64, updatable = false, unique = true)
    private String deduplicationKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;
    @Column(name = "locked_at")
    private Instant lockedAt;
    @Column(name = "locked_by", length = 128)
    private String lockedBy;
    @Column(name = "last_error", length = 2000)
    private String lastError;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "processed_at")
    private Instant processedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    @Column(nullable = false)
    private long version;
}
