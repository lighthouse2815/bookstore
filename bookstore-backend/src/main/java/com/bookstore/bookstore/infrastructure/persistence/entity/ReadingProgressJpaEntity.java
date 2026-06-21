package com.bookstore.bookstore.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reading_progresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reading_progresses_user_asset",
                columnNames = {"user_id", "digital_asset_id"}
        ),
        indexes = {
                @Index(name = "idx_reading_progresses_user_id", columnList = "user_id"),
                @Index(name = "idx_reading_progresses_digital_asset_id", columnList = "digital_asset_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ReadingProgressJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "digital_asset_id", nullable = false)
    private DigitalAssetJpaEntity digitalAsset;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "progress_percent", precision = 5, scale = 2)
    private BigDecimal progressPercent;

    @Lob
    @Column(name = "position_data", columnDefinition = "TEXT")
    private String positionData;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
