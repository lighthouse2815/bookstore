package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserAuthIdentity {

    private UUID id;
    private UUID userId;
    private AuthProvider provider;
    private String providerSubject;
    private String providerEmail;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;

    public UserAuthIdentity(
            UUID id,
            UUID userId,
            AuthProvider provider,
            String providerSubject,
            String providerEmail,
            boolean emailVerified,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_USER_AUTH_IDENTITY_ID, "id");
        setUserId(userId);
        setProvider(provider);
        setProviderSubject(providerSubject);
        setProviderEmail(providerEmail);
        setEmailVerified(emailVerified);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    public void syncProviderState(String providerEmail, boolean emailVerified) {
        setProviderEmail(providerEmail);
        setEmailVerified(emailVerified);
        setUpdatedAt(Instant.now());
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_USER_AUTH_IDENTITY_USER_ID, "userId");
    }

    private void setProvider(AuthProvider provider) {
        this.provider = Guard.notNull(provider, DomainErrorCode.INVALID_USER_AUTH_IDENTITY_PROVIDER, "provider");
    }

    private void setProviderSubject(String providerSubject) {
        this.providerSubject = Guard.notBlank(
                providerSubject,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_PROVIDER_SUBJECT,
                "providerSubject"
        );
    }

    private void setProviderEmail(String providerEmail) {
        this.providerEmail = Guard.emailOrNull(
                providerEmail,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_PROVIDER_EMAIL,
                "providerEmail"
        );
    }

    private void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                null,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_CREATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_UPDATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_UPDATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFuture(
                updatedAt,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                null,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_CREATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_UPDATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_UPDATED_AT,
                DomainErrorCode.INVALID_USER_AUTH_IDENTITY_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }
}
