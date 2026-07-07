package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "file_assets",
        indexes = {
                @Index(name = "idx_file_assets_status", columnList = "status"),
                @Index(name = "idx_file_assets_purpose", columnList = "purpose"),
                @Index(name = "idx_file_assets_storage_key", columnList = "storage_key"),
                @Index(name = "idx_file_assets_created_by", columnList = "created_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class FileAssetJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FileProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FilePurpose purpose;

    @Column(length = 255)
    private String bucket;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "public_url", columnDefinition = "TEXT")
    private String publicUrl;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "checksum_sha256", length = 128)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FileVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FileStatus status;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
