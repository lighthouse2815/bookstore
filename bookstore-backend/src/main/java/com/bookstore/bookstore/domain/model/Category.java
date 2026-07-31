package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.CategoryRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Category {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Map<CategoryLocale, CategoryTranslation> translations;
    private UUID parentId;
    private FileAsset imageFileAsset;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Category(
            UUID id,
            String code,
            String name,
            String description,
            Map<CategoryLocale, CategoryTranslation> translations,
            UUID parentId,
            FileAsset imageFileAsset,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_CATEGORY_ID, "id");
        setCode(code);
        setName(name);
        setDescription(description);
        setTranslations(translations);
        setParentId(parentId);
        setImageFileAsset(imageFileAsset);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public Category(
            UUID id,
            String name,
            String description,
            UUID parentId,
            FileAsset imageFileAsset,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this(
                id,
                "LEGACY_" + id.toString().replace("-", "").toUpperCase(),
                name,
                description,
                Map.of(
                        CategoryLocale.VI,
                        new CategoryTranslation(CategoryLocale.VI, name, description)
                ),
                parentId,
                imageFileAsset,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public void updateCategory(
            String code,
            String name,
            String description,
            Map<CategoryLocale, CategoryTranslation> translations,
            UUID parentId,
            FileAsset imageFileAsset
    ) {
        CategoryRule.requireCanUpdate(
                deletedAt,
                this.code,
                this.name,
                this.description,
                this.translations,
                this.parentId,
                getImageFileAssetId(),
                code,
                name,
                description,
                translations,
                parentId,
                imageFileAsset == null ? null : imageFileAsset.getId()
        );
        setCode(code);
        setName(name);
        setDescription(description);
        setTranslations(translations);
        setParentId(parentId);
        setImageFileAsset(imageFileAsset);
        setUpdatedAt(Instant.now());
    }

    private void setCode(String code) {
        this.code = Guard.notBlank(code, DomainErrorCode.INVALID_CATEGORY_CODE, "code");
    }

    public void softDelete() {
        CategoryRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setName(String name) {
        this.name = Guard.notBlank(name, DomainErrorCode.INVALID_CATEGORY_NAME, "name");
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setTranslations(Map<CategoryLocale, CategoryTranslation> translations) {
        if (translations == null
                || translations.isEmpty()
                || translations.entrySet().stream().anyMatch(entry ->
                        entry.getKey() == null
                                || entry.getValue() == null
                                || entry.getKey() != entry.getValue().locale()
                )) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_CATEGORY_TRANSLATION,
                    "translations"
            );
        }
        this.translations = Map.copyOf(new LinkedHashMap<>(translations));
    }

    private void setParentId(UUID parentId) {
        if (parentId != null && parentId.equals(id)) {
            throw new com.bookstore.bookstore.domain.exception.DomainException(
                    DomainErrorCode.INVALID_CATEGORY_PARENT_ID,
                    "parentId"
            );
        }
        this.parentId = parentId;
    }

    public UUID getImageFileAssetId() {
        return imageFileAsset == null ? null : imageFileAsset.getId();
    }

    public String getImageUrl() {
        return imageFileAsset == null ? null : imageFileAsset.getPublicUrl();
    }

    private void setImageFileAsset(FileAsset imageFileAsset) {
        this.imageFileAsset = imageFileAsset;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_CATEGORY_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CATEGORY_CREATED_AT,
                DomainErrorCode.INVALID_CATEGORY_UPDATED_AT,
                DomainErrorCode.INVALID_CATEGORY_DELETED_AT,
                DomainErrorCode.INVALID_CATEGORY_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_CATEGORY_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_CATEGORY_CREATED_AT,
                DomainErrorCode.INVALID_CATEGORY_UPDATED_AT,
                DomainErrorCode.INVALID_CATEGORY_DELETED_AT,
                DomainErrorCode.INVALID_CATEGORY_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_CATEGORY_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_CATEGORY_CREATED_AT,
                DomainErrorCode.INVALID_CATEGORY_UPDATED_AT,
                DomainErrorCode.INVALID_CATEGORY_DELETED_AT,
                DomainErrorCode.INVALID_CATEGORY_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
