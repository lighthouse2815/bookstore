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
    private FileAsset fileAsset;
    private FileAsset sampleFileAsset;
    private BigDecimal price;
    private boolean downloadAllowed;
    private boolean purchaseAllowed;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public DigitalAsset(
            UUID id,
            UUID bookId,
            DigitalAssetFormat format,
            String title,
            FileAsset fileAsset,
            FileAsset sampleFileAsset,
            BigDecimal price,
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_DIGITAL_ASSET_ID, "id");
        setBookId(bookId);
        setFormat(format);
        setTitle(title);
        setFileAsset(fileAsset);
        setSampleFileAsset(sampleFileAsset);
        setPrice(price);
        setDownloadAllowed(downloadAllowed);
        setPurchaseAllowed(purchaseAllowed);
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
            FileAsset fileAsset,
            FileAsset sampleFileAsset,
            BigDecimal price,
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published
    ) {
        setFormat(format);
        setTitle(title);
        setFileAsset(fileAsset);
        setSampleFileAsset(sampleFileAsset);
        setPrice(price);
        setDownloadAllowed(downloadAllowed);
        setPurchaseAllowed(purchaseAllowed);
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

    public UUID getFileAssetId() {
        return fileAsset.getId();
    }

    public UUID getSampleFileAssetId() {
        return sampleFileAsset == null ? null : sampleFileAsset.getId();
    }

    public String getFileName() {
        return fileAsset.getOriginalName();
    }

    public String getStorageKey() {
        return fileAsset.getStorageKey();
    }

    public String getMimeType() {
        return fileAsset.getContentType();
    }

    public Long getFileSize() {
        return fileAsset.getSizeBytes();
    }

    public String getChecksum() {
        return fileAsset.getChecksumSha256();
    }

    public String getSampleStorageKey() {
        return sampleFileAsset == null ? null : sampleFileAsset.getStorageKey();
    }

    public String getFileUrl() {
        return fileAsset.getPublicUrl();
    }

    public String getSampleFileUrl() {
        return sampleFileAsset == null ? null : sampleFileAsset.getPublicUrl();
    }

    private void setFileAsset(FileAsset fileAsset) {
        this.fileAsset = Guard.notNull(
                fileAsset,
                DomainErrorCode.INVALID_DIGITAL_ASSET_FILE_ASSET_ID,
                "fileAssetId"
        );
    }

    private void setSampleFileAsset(FileAsset sampleFileAsset) {
        if (sampleFileAsset == null) {
            this.sampleFileAsset = null;
            return;
        }

        this.sampleFileAsset = Guard.notNull(
                sampleFileAsset,
                DomainErrorCode.INVALID_DIGITAL_ASSET_SAMPLE_FILE_ASSET_ID,
                "sampleFileAssetId"
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

    private void setPurchaseAllowed(boolean purchaseAllowed) {
        this.purchaseAllowed = purchaseAllowed;
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
