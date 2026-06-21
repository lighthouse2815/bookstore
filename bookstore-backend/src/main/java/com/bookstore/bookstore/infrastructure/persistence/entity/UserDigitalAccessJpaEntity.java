package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_digital_accesses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_digital_accesses_user_asset_type",
                columnNames = {"user_id", "digital_asset_id", "access_type"}
        ),
        indexes = {
                @Index(name = "idx_user_digital_accesses_user_id", columnList = "user_id"),
                @Index(name = "idx_user_digital_accesses_digital_asset_id", columnList = "digital_asset_id"),
                @Index(name = "idx_user_digital_accesses_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserDigitalAccessJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "digital_asset_id", nullable = false)
    private DigitalAssetJpaEntity digitalAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 32)
    private DigitalAccessType accessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DigitalAccessStatus status;

    @Column(name = "source_order_id")
    private UUID sourceOrderId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
