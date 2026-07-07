package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ProfileRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Profile {

    private UUID id;
    private UUID userId;
    private String lastName;
    private String firstName;
    private FileAsset avatarFileAsset;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Profile(
            UUID id,
            UUID userId,
            String lastName,
            String firstName,
            FileAsset avatarFileAsset,
            Gender gender,
            LocalDate dateOfBirth,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PROFILE_ID, "id");
        setUserId(userId);
        setLastName(lastName);
        setFirstName(firstName);
        setAvatarFileAsset(avatarFileAsset);
        setGender(gender);
        setDateOfBirth(dateOfBirth);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateProfileInfo(
            String lastName,
            String firstName,
            FileAsset avatarFileAsset,
            Gender gender,
            LocalDate dateOfBirth
    ) {
        ProfileRule.requireCanUpdateProfileInfo(deletedAt);
        Instant now = Instant.now();

        setRequiredLastName(lastName);
        setRequiredFirstName(firstName);
        setAvatarFileAsset(avatarFileAsset);
        setRequiredGender(gender);
        setRequiredDateOfBirth(dateOfBirth);
        setUpdatedAt(now);
    }

    public void softDelete() {
        ProfileRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_PROFILE_USER_ID, "userId");
    }

    private void setLastName(String lastName) {
        this.lastName = Guard.notBlankOrNull(lastName, DomainErrorCode.INVALID_PROFILE_LAST_NAME, "lastName");
    }

    private void setRequiredLastName(String lastName) {
        this.lastName = Guard.notBlank(lastName, DomainErrorCode.INVALID_PROFILE_LAST_NAME, "lastName");
    }

    private void setFirstName(String firstName) {
        this.firstName = Guard.notBlankOrNull(firstName, DomainErrorCode.INVALID_PROFILE_FIRST_NAME, "firstName");
    }

    private void setRequiredFirstName(String firstName) {
        this.firstName = Guard.notBlank(firstName, DomainErrorCode.INVALID_PROFILE_FIRST_NAME, "firstName");
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
                DomainErrorCode.INVALID_PROFILE_AVATAR_FILE_ASSET_ID,
                "avatarFileAssetId"
        );
    }

    private void setGender(Gender gender) {
        this.gender = gender;
    }

    private void setRequiredGender(Gender gender) {
        this.gender = Guard.notNull(gender, DomainErrorCode.INVALID_PROFILE_GENDER, "gender");
    }

    private void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = Guard.notInFutureOrNull(
                dateOfBirth,
                DomainErrorCode.INVALID_PROFILE_DATE_OF_BIRTH,
                "dateOfBirth"
        );
    }

    private void setRequiredDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = Guard.notInFuture(
                dateOfBirth,
                DomainErrorCode.INVALID_PROFILE_DATE_OF_BIRTH,
                "dateOfBirth"
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(createdAt, DomainErrorCode.INVALID_PROFILE_CREATED_AT, "createdAt");
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PROFILE_CREATED_AT,
                DomainErrorCode.INVALID_PROFILE_UPDATED_AT,
                DomainErrorCode.INVALID_PROFILE_DELETED_AT,
                DomainErrorCode.INVALID_PROFILE_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_PROFILE_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PROFILE_CREATED_AT,
                DomainErrorCode.INVALID_PROFILE_UPDATED_AT,
                DomainErrorCode.INVALID_PROFILE_DELETED_AT,
                DomainErrorCode.INVALID_PROFILE_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_PROFILE_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_PROFILE_CREATED_AT,
                DomainErrorCode.INVALID_PROFILE_UPDATED_AT,
                DomainErrorCode.INVALID_PROFILE_DELETED_AT,
                DomainErrorCode.INVALID_PROFILE_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
