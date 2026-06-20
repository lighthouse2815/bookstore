package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DigitalAsset {

    private UUID id;
    private UUID bookId;
    private DigitalAssetFormat format;
    private String title;
    private String fileName;
    private String storageKey;
    private String mimeType;
    private Long fileSize;
    private String checksum;
    private String sampleStorageKey;
    private BigDecimal price;
    private boolean downloadAllowed;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public DigitalAsset(
            UUID id,
            UUID bookId,
            DigitalAssetFormat format,
            String title,
            String fileName,
            String storageKey,
            String mimeType,
            Long fileSize,
            String checksum,
            String sampleStorageKey,
            BigDecimal price,
            boolean downloadAllowed,
            boolean published,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_DIGITAL_ASSET_ID, "id");
        setBookId(bookId);
        setFormat(format);
        setTitle(title);
        setFileName(fileName);
        setStorageKey(storageKey);
        setMimeType(mimeType);
        setFileSize(fileSize);
        setChecksum(checksum);
        setSampleStorageKey(sampleStorageKey);
        setPrice(price);
        setDownloadAllowed(downloadAllowed);
        setPublished(published);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void softDelete() {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DIGITAL_ASSET_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        setPublished(false);
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void updateAsset(
            DigitalAssetFormat format,
            String title,
            String fileName,
            String storageKey,
            String mimeType,
            Long fileSize,
            String checksum,
            String sampleStorageKey,
            BigDecimal price,
            boolean downloadAllowed,
            boolean published
    ) {
        setFormat(format);
        setTitle(title);
        setFileName(fileName);
        setStorageKey(storageKey);
        setMimeType(mimeType);
        setFileSize(fileSize);
        setChecksum(checksum);
        setSampleStorageKey(sampleStorageKey);
        setPrice(price);
        setDownloadAllowed(downloadAllowed);
        setPublished(published);
        setUpdatedAt(Instant.now());
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_DIGITAL_ASSET_BOOK_ID, "bookId");
    }

    private void setFormat(DigitalAssetFormat format) {
        this.format = Guard.notNull(format, DomainErrorCode.INVALID_DIGITAL_ASSET_FORMAT, "format");
    }

    private void setTitle(String title) {
        this.title = Guard.notBlank(title, DomainErrorCode.INVALID_DIGITAL_ASSET_TITLE, "title");
    }

    private void setFileName(String fileName) {
        this.fileName = Guard.notBlank(fileName, DomainErrorCode.INVALID_DIGITAL_ASSET_FILE_NAME, "fileName");
    }

    private void setStorageKey(String storageKey) {
        this.storageKey = Guard.notBlank(storageKey, DomainErrorCode.INVALID_DIGITAL_ASSET_STORAGE_KEY, "storageKey");
    }

    private void setMimeType(String mimeType) {
        this.mimeType = Guard.notBlank(mimeType, DomainErrorCode.INVALID_DIGITAL_ASSET_MIME_TYPE, "mimeType");
    }

    private void setFileSize(Long fileSize) {
        Long validFileSize = Guard.notNull(fileSize, DomainErrorCode.INVALID_DIGITAL_ASSET_FILE_SIZE, "fileSize");
        if (validFileSize < 0) {
            throw new DomainException(DomainErrorCode.INVALID_DIGITAL_ASSET_FILE_SIZE, "fileSize");
        }
        this.fileSize = validFileSize;
    }

    private void setChecksum(String checksum) {
        this.checksum = Guard.notBlankOrNull(checksum, DomainErrorCode.INVALID_DIGITAL_ASSET_CHECKSUM, "checksum");
    }

    private void setSampleStorageKey(String sampleStorageKey) {
        this.sampleStorageKey = Guard.notBlankOrNull(
                sampleStorageKey,
                DomainErrorCode.INVALID_DIGITAL_ASSET_SAMPLE_STORAGE_KEY,
                "sampleStorageKey"
        );
    }

    private void setPrice(BigDecimal price) {
        BigDecimal validPrice = Guard.notNull(price, DomainErrorCode.INVALID_DIGITAL_ASSET_PRICE, "price");
        if (validPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_DIGITAL_ASSET_PRICE, "price");
        }
        this.price = validPrice;
    }

    private void setDownloadAllowed(boolean downloadAllowed) {
        this.downloadAllowed = downloadAllowed;
    }

    private void setPublished(boolean published) {
        this.published = published;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_DIGITAL_ASSET_CREATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_UPDATED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_DELETED_AT,
                DomainErrorCode.INVALID_DIGITAL_ASSET_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
