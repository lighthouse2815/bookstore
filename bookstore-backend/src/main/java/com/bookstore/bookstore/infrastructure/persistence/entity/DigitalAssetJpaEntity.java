package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "digital_assets",
        indexes = {
                @Index(name = "idx_digital_assets_book_id", columnList = "book_id"),
                @Index(name = "idx_digital_assets_format", columnList = "format"),
                @Index(name = "idx_digital_assets_published", columnList = "published")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DigitalAssetJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DigitalAssetFormat format;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_asset_id")
    private FileAssetJpaEntity fileAsset;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 128)
    private String checksum;

    @Column(name = "sample_storage_key", length = 500)
    private String sampleStorageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_file_asset_id")
    private FileAssetJpaEntity sampleFileAsset;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "download_allowed", nullable = false)
    private boolean downloadAllowed;

    @Column(name = "purchase_allowed")
    private Boolean purchaseAllowed;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
