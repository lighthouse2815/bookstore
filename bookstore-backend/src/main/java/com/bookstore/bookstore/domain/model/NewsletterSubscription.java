package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.NewsletterSubscriptionStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class NewsletterSubscription {

    private UUID id;
    private String email;
    private NewsletterSubscriptionStatus status;
    private String unsubscribeToken;
    private Instant subscribedAt;
    private Instant unsubscribedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public NewsletterSubscription(
            UUID id,
            String email,
            NewsletterSubscriptionStatus status,
            String unsubscribeToken,
            Instant subscribedAt,
            Instant unsubscribedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_NEWSLETTER_SUBSCRIPTION_ID, "id");
        this.email = Guard.email(email, DomainErrorCode.INVALID_NEWSLETTER_SUBSCRIPTION_EMAIL, "email");
        this.status = Guard.notNull(
                status,
                DomainErrorCode.INVALID_NEWSLETTER_SUBSCRIPTION_STATUS,
                "status"
        );
        this.unsubscribeToken = Guard.notBlank(
                unsubscribeToken,
                DomainErrorCode.INVALID_NEWSLETTER_UNSUBSCRIBE_TOKEN,
                "unsubscribeToken"
        );
        this.subscribedAt = Guard.notInFuture(
                subscribedAt,
                DomainErrorCode.INVALID_NEWSLETTER_SUBSCRIBED_AT,
                "subscribedAt"
        );
        this.unsubscribedAt = Guard.notInFutureOrNull(
                unsubscribedAt,
                DomainErrorCode.INVALID_NEWSLETTER_UNSUBSCRIBED_AT,
                "unsubscribedAt"
        );
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void reactivate(String nextUnsubscribeToken) {
        Instant now = Instant.now();
        this.status = NewsletterSubscriptionStatus.ACTIVE;
        this.unsubscribeToken = Guard.notBlank(
                nextUnsubscribeToken,
                DomainErrorCode.INVALID_NEWSLETTER_UNSUBSCRIBE_TOKEN,
                "unsubscribeToken"
        );
        this.subscribedAt = now;
        this.unsubscribedAt = null;
        this.updatedAt = now;
    }

    public void unsubscribe() {
        if (status == NewsletterSubscriptionStatus.UNSUBSCRIBED) {
            return;
        }

        Instant now = Instant.now();
        this.status = NewsletterSubscriptionStatus.UNSUBSCRIBED;
        this.unsubscribedAt = now;
        this.updatedAt = now;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_NEWSLETTER_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_NEWSLETTER_CREATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_UPDATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_DELETED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_NEWSLETTER_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_NEWSLETTER_CREATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_UPDATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_DELETED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_NEWSLETTER_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_NEWSLETTER_CREATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_UPDATED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_DELETED_AT,
                DomainErrorCode.INVALID_NEWSLETTER_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
