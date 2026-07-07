package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.rule.AuthorRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Author {

    private UUID id;
    private String name;
    private String biography;
    private FileAsset avatarFileAsset;
    private Integer birthYear;
    private Integer deathYear;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Author(
            UUID id,
            String name,
            String biography,
            FileAsset avatarFileAsset,
            Integer birthYear,
            Integer deathYear,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_AUTHOR_ID, "id");
        setName(name);
        setBiography(biography);
        setAvatarFileAsset(avatarFileAsset);
        setBirthYear(birthYear);
        setDeathYear(deathYear);
        validateLifeSpan();
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateAuthor(
            String name,
            String biography,
            FileAsset avatarFileAsset,
            Integer birthYear,
            Integer deathYear
    ) {
        AuthorRule.requireCanUpdate(
                deletedAt,
                this.name,
                this.biography,
                getAvatarFileAssetId(),
                this.birthYear,
                this.deathYear,
                name,
                biography,
                avatarFileAsset == null ? null : avatarFileAsset.getId(),
                birthYear,
                deathYear
        );
        setName(name);
        setBiography(biography);
        setAvatarFileAsset(avatarFileAsset);
        setBirthYear(birthYear);
        setDeathYear(deathYear);
        validateLifeSpan();
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        AuthorRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setName(String name) {
        this.name = Guard.notBlank(name, DomainErrorCode.INVALID_AUTHOR_NAME, "name");
    }

    private void setBiography(String biography) {
        this.biography = biography;
    }

    public UUID getAvatarFileAssetId() {
        return avatarFileAsset == null ? null : avatarFileAsset.getId();
    }

    public String getAvatarUrl() {
        return avatarFileAsset == null ? null : avatarFileAsset.getPublicUrl();
    }

    private void setAvatarFileAsset(FileAsset avatarFileAsset) {
        if (avatarFileAsset == null) {
            this.avatarFileAsset = null;
            return;
        }

        this.avatarFileAsset = Guard.notNull(
                avatarFileAsset,
                DomainErrorCode.INVALID_AUTHOR_AVATAR_FILE_ASSET_ID,
                "avatarFileAssetId"
        );
    }

    private void setBirthYear(Integer birthYear) {
        if (birthYear != null && birthYear <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_AUTHOR_BIRTH_YEAR, "birthYear");
        }
        this.birthYear = birthYear;
    }

    private void setDeathYear(Integer deathYear) {
        if (deathYear != null && deathYear <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_AUTHOR_DEATH_YEAR, "deathYear");
        }
        this.deathYear = deathYear;
    }

    private void validateLifeSpan() {
        if (birthYear != null && deathYear != null && deathYear < birthYear) {
            throw new DomainException(DomainErrorCode.INVALID_AUTHOR_LIFESPAN);
        }
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(createdAt, DomainErrorCode.INVALID_AUTHOR_CREATED_AT, "createdAt");
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_AUTHOR_CREATED_AT,
                DomainErrorCode.INVALID_AUTHOR_UPDATED_AT,
                DomainErrorCode.INVALID_AUTHOR_DELETED_AT,
                DomainErrorCode.INVALID_AUTHOR_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_AUTHOR_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_AUTHOR_CREATED_AT,
                DomainErrorCode.INVALID_AUTHOR_UPDATED_AT,
                DomainErrorCode.INVALID_AUTHOR_DELETED_AT,
                DomainErrorCode.INVALID_AUTHOR_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_AUTHOR_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_AUTHOR_CREATED_AT,
                DomainErrorCode.INVALID_AUTHOR_UPDATED_AT,
                DomainErrorCode.INVALID_AUTHOR_DELETED_AT,
                DomainErrorCode.INVALID_AUTHOR_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
