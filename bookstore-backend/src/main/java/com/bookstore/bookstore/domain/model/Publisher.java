package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.PublisherRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Publisher {

    private UUID id;
    private String name;
    private String description;
    private FileAsset logoFileAsset;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Publisher(
            UUID id,
            String name,
            String description,
            FileAsset logoFileAsset,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PUBLISHER_ID, "id");
        setName(name);
        setDescription(description);
        setLogoFileAsset(logoFileAsset);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updatePublisher(String name, String description, FileAsset logoFileAsset) {
        PublisherRule.requireCanUpdate(
                deletedAt,
                this.name,
                this.description,
                getLogoFileAssetId(),
                name,
                description,
                logoFileAsset == null ? null : logoFileAsset.getId()
        );
        setName(name);
        setDescription(description);
        setLogoFileAsset(logoFileAsset);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        PublisherRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setName(String name) {
        this.name = Guard.notBlank(name, DomainErrorCode.INVALID_PUBLISHER_NAME, "name");
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public UUID getLogoFileAssetId() {
        return logoFileAsset == null ? null : logoFileAsset.getId();
    }

    public String getLogoUrl() {
        return logoFileAsset == null ? null : logoFileAsset.getPublicUrl();
    }

    private void setLogoFileAsset(FileAsset logoFileAsset) {
        this.logoFileAsset = logoFileAsset;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_PUBLISHER_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PUBLISHER_CREATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_UPDATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_DELETED_AT,
                DomainErrorCode.INVALID_PUBLISHER_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_PUBLISHER_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PUBLISHER_CREATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_UPDATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_DELETED_AT,
                DomainErrorCode.INVALID_PUBLISHER_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_PUBLISHER_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_PUBLISHER_CREATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_UPDATED_AT,
                DomainErrorCode.INVALID_PUBLISHER_DELETED_AT,
                DomainErrorCode.INVALID_PUBLISHER_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
