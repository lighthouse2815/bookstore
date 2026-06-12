package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.UserRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

@Getter
public class User {

    private UUID id;
    private String username;
    private String passwordHash;
    private String phoneNumber;
    private String email;
    private UserStatus status;
    private boolean locked;
    private Set<Role> roles = new LinkedHashSet<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public User(
            UUID id,
            String username,
            String passwordHash,
            String phoneNumber,
            String email,
            UserStatus status,
            boolean locked,
            Set<Role> roles,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_USER_ID, "id");
        setUsername(username);
        setPasswordHash(passwordHash);
        setPhoneNumber(phoneNumber);
        setEmail(email);
        setStatus(status);
        setLocked(locked);
        setRoles(roles);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void activate() {
        UserRule.requireCanActivate(status, locked, deletedAt);
        setStatus(UserStatus.ACTIVE);
        setUpdatedAt(Instant.now());
    }

    public void updateAccountInfo(String username, String email, String phoneNumber) {
        UserRule.requireCanUpdateAccountInfo(
            status,
            locked, 
            deletedAt,
            this.username,
            this.email,
            this.phoneNumber,
            username,
            email,
            phoneNumber
        );

        setUsername(username);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setUpdatedAt(Instant.now());
    }

    public void updateManagedInfo(
            String email,
            String phoneNumber,
            Set<Role> roles
    ) {
        UserRule.requireCanUpdateManagedInfo(
                deletedAt,
                this.email,
                this.phoneNumber,
                this.roles,
                email,
                phoneNumber,
                roles
        );
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setRoles(roles);
        setUpdatedAt(Instant.now());
    }

    public void updateLockStatus(boolean locked) {
        UserRule.requireCanUpdateLockStatus(deletedAt);
        UserRule.requireLockStatusChanged(this.locked, locked);
        setLocked(locked);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        UserRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();

        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void requireCanLogin() {
        UserRule.requireCanLogin(status, locked, deletedAt);
    }

    public void updatePasswordHash(String passwordHash) {
        setPasswordHash(passwordHash);
        setUpdatedAt(Instant.now());
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }
    public boolean hasRole(String roleName) {
        if (roleName == null || roles == null) {
            return false;
        }

        return roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }


    private void setUsername(String username) {
        this.username = Guard.notBlank(username, DomainErrorCode.INVALID_USER_USERNAME, "username");
    }

    private void setPasswordHash(String passwordHash) {
        this.passwordHash = Guard.notBlankOrNull(passwordHash, DomainErrorCode.INVALID_USER_PASSWORD_HASH, "passwordHash");
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = Guard.phoneNumberOrNull(phoneNumber, DomainErrorCode.INVALID_USER_PHONE_NUMBER, "phoneNumber");
    }

    private void setEmail(String email) {
        this.email = Guard.email(email, DomainErrorCode.INVALID_USER_EMAIL, "email");
    }

    private void setStatus(UserStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_USER_STATUS, "status");
    }

    private void setLocked(boolean locked) {
        this.locked = locked;
    }

    private void setRoles(Set<Role> roles) {
        this.roles = new LinkedHashSet<>(Guard.noNullElements(roles, DomainErrorCode.INVALID_USER_ROLES, "roles"));
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(createdAt, DomainErrorCode.INVALID_USER_CREATED_AT, "createdAt");
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_CREATED_AT,
                DomainErrorCode.INVALID_USER_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DELETED_AT,
                DomainErrorCode.INVALID_USER_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_USER_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_CREATED_AT,
                DomainErrorCode.INVALID_USER_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DELETED_AT,
                DomainErrorCode.INVALID_USER_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_USER_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_USER_CREATED_AT,
                DomainErrorCode.INVALID_USER_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DELETED_AT,
                DomainErrorCode.INVALID_USER_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }

}
