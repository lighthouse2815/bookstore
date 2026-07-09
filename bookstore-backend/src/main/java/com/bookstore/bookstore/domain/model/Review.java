package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ReviewRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Review {

    private UUID id;
    private UUID userId;
    private UUID bookId;
    private UUID orderItemId;
    private int rating;
    private String comment;
    private ReviewStatus status;
    private String moderationReason;
    private UUID moderatedBy;
    private Instant moderatedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Review(
            UUID id,
            UUID userId,
            UUID bookId,
            UUID orderItemId,
            int rating,
            String comment,
            ReviewStatus status,
            String moderationReason,
            UUID moderatedBy,
            Instant moderatedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_REVIEW_ID, "id");
        setUserId(userId);
        setBookId(bookId);
        setOrderItemId(orderItemId);
        setRating(rating);
        setComment(comment);
        setStatus(status);
        setModerationReason(moderationReason);
        setModeratedBy(moderatedBy);
        setModeratedAt(moderatedAt);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateReview(int rating, String comment) {
        ReviewRule.requireCanUpdate(deletedAt);
        setRating(rating);
        setComment(comment);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        ReviewRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void hide(String moderationReason, UUID moderatedBy) {
        ReviewRule.requireCanModerate(deletedAt);
        Instant now = Instant.now();
        setStatus(ReviewStatus.HIDDEN);
        setModerationReason(moderationReason);
        setModeratedBy(moderatedBy);
        setModeratedAt(now);
        setUpdatedAt(now);
    }

    public void approve(UUID moderatedBy) {
        ReviewRule.requireCanModerate(deletedAt);
        Instant now = Instant.now();
        setStatus(ReviewStatus.APPROVED);
        setModerationReason(null);
        setModeratedBy(moderatedBy);
        setModeratedAt(now);
        setUpdatedAt(now);
    }

    public boolean isPubliclyVisible() {
        return deletedAt == null && status == ReviewStatus.APPROVED;
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_REVIEW_USER_ID, "userId");
    }

    private void setBookId(UUID bookId) {
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_REVIEW_BOOK_ID, "bookId");
    }

    private void setOrderItemId(UUID orderItemId) {
        this.orderItemId = Guard.notNull(orderItemId, DomainErrorCode.INVALID_REVIEW_ORDER_ITEM_ID, "orderItemId");
    }

    private void setRating(int rating) {
        ReviewRule.requireValidRating(rating);
        this.rating = rating;
    }

    private void setComment(String comment) {
        ReviewRule.requireValidCommentLength(comment);
        this.comment = comment;
    }

    private void setStatus(ReviewStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_REVIEW_STATUS, "status");
    }

    private void setModerationReason(String moderationReason) {
        ReviewRule.requireValidModerationReason(moderationReason);
        this.moderationReason = moderationReason;
    }

    private void setModeratedBy(UUID moderatedBy) {
        this.moderatedBy = moderatedBy;
    }

    private void setModeratedAt(Instant moderatedAt) {
        this.moderatedAt = Guard.notInFutureOrNull(
                moderatedAt,
                DomainErrorCode.INVALID_REVIEW_MODERATED_AT,
                "moderatedAt"
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_REVIEW_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_REVIEW_CREATED_AT,
                DomainErrorCode.INVALID_REVIEW_UPDATED_AT,
                DomainErrorCode.INVALID_REVIEW_DELETED_AT,
                DomainErrorCode.INVALID_REVIEW_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_REVIEW_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_REVIEW_CREATED_AT,
                DomainErrorCode.INVALID_REVIEW_UPDATED_AT,
                DomainErrorCode.INVALID_REVIEW_DELETED_AT,
                DomainErrorCode.INVALID_REVIEW_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_REVIEW_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_REVIEW_CREATED_AT,
                DomainErrorCode.INVALID_REVIEW_UPDATED_AT,
                DomainErrorCode.INVALID_REVIEW_DELETED_AT,
                DomainErrorCode.INVALID_REVIEW_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
