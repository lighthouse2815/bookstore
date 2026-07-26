package com.bookstore.bookstore.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_login_attempts", indexes = {
        @Index(name = "idx_auth_login_attempts_locked_until", columnList = "locked_until"),
        @Index(name = "idx_auth_login_attempts_updated_at", columnList = "updated_at")
})
@Getter
@Setter
@NoArgsConstructor
public class AuthLoginAttemptJpaEntity {

    @Id
    private UUID id;

    @Column(name = "attempt_type", nullable = false, length = 32)
    private String attemptType;

    @Column(name = "subject_hash", nullable = false, length = 64)
    private String subjectHash;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "window_started_at", nullable = false)
    private Instant windowStartedAt;

    @Column(name = "last_failed_at", nullable = false)
    private Instant lastFailedAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
