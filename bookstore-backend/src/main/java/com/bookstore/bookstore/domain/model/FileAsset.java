package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FileAsset {

    private UUID id;
    private FileProvider provider;
    private FilePurpose purpose;
    private String bucket;
    private String storageKey;
    private String publicUrl;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private String checksumSha256;
    private FileVisibility visibility;
    private FileStatus status;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public FileAsset(
            UUID id,
            FileProvider provider,
            FilePurpose purpose,
            String bucket,
            String storageKey,
            String publicUrl,
            String originalName,
            String contentType,
            Long sizeBytes,
            String checksumSha256,
            FileVisibility visibility,
            FileStatus status,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_FILE_ASSET_ID, "id");
        this.provider = Guard.notNull(provider, DomainErrorCode.INVALID_FILE_ASSET_PROVIDER, "provider");
        this.purpose = Guard.notNull(purpose, DomainErrorCode.INVALID_FILE_ASSET_PURPOSE, "purpose");
        this.bucket = Guard.notBlankOrNull(bucket, DomainErrorCode.INVALID_FILE_ASSET_BUCKET, "bucket");
        this.storageKey = Guard.notBlank(storageKey, DomainErrorCode.INVALID_FILE_ASSET_STORAGE_KEY, "storageKey");
        this.publicUrl = Guard.notBlankOrNull(publicUrl, DomainErrorCode.INVALID_FILE_ASSET_PUBLIC_URL, "publicUrl");
        this.originalName = Guard.notBlankOrNull(
                originalName,
                DomainErrorCode.INVALID_FILE_ASSET_ORIGINAL_NAME,
                "originalName"
        );
        this.contentType = Guard.notBlankOrNull(
                contentType,
                DomainErrorCode.INVALID_FILE_ASSET_CONTENT_TYPE,
                "contentType"
        );
        setSizeBytes(sizeBytes);
        this.checksumSha256 = Guard.notBlankOrNull(
                checksumSha256,
                DomainErrorCode.INVALID_FILE_ASSET_CHECKSUM_SHA256,
                "checksumSha256"
        );
        this.visibility = Guard.notNull(
                visibility,
                DomainErrorCode.INVALID_FILE_ASSET_VISIBILITY,
                "visibility"
        );
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_FILE_ASSET_STATUS, "status");
        this.createdBy = Guard.notNull(createdBy, DomainErrorCode.INVALID_FILE_ASSET_CREATED_BY, "createdBy");
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void activate(String checksumSha256) {
        if (status == FileStatus.DELETED) {
            throw new DomainException(DomainErrorCode.FILE_ASSET_ALREADY_DELETED);
        }
        this.checksumSha256 = Guard.notBlankOrNull(
                checksumSha256,
                DomainErrorCode.INVALID_FILE_ASSET_CHECKSUM_SHA256,
                "checksumSha256"
        );
        status = FileStatus.ACTIVE;
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        if (status == FileStatus.DELETED || deletedAt != null) {
            throw new DomainException(DomainErrorCode.FILE_ASSET_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        status = FileStatus.DELETED;
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public boolean isActive() {
        return status == FileStatus.ACTIVE && deletedAt == null;
    }

    public boolean isPublic() {
        return visibility == FileVisibility.PUBLIC;
    }

    private void setSizeBytes(Long sizeBytes) {
        if (sizeBytes != null && sizeBytes < 0) {
            throw new DomainException(DomainErrorCode.INVALID_FILE_ASSET_SIZE_BYTES, "sizeBytes");
        }
        this.sizeBytes = sizeBytes;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_FILE_ASSET_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_FILE_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_FILE_ASSET_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_FILE_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_FILE_ASSET_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_FILE_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_FILE_ASSET_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
