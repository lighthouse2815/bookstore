package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReturnRequest {

    private UUID id;
    private UUID orderId;
    private UUID userId;
    private String reason;
    private ReturnRequestStatus status;
    private String adminNote;
    private BigDecimal requestedRefundAmount;
    private BigDecimal approvedRefundAmount;
    private UUID processedBy;
    private Instant processedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public ReturnRequest(
            UUID id,
            UUID orderId,
            UUID userId,
            String reason,
            ReturnRequestStatus status,
            String adminNote,
            BigDecimal requestedRefundAmount,
            BigDecimal approvedRefundAmount,
            UUID processedBy,
            Instant processedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_RETURN_REQUEST_ID, "id");
        setOrderId(orderId);
        setUserId(userId);
        setReason(reason);
        setStatus(status);
        setAdminNote(adminNote);
        setRequestedRefundAmount(requestedRefundAmount);
        setApprovedRefundAmount(approvedRefundAmount);
        setProcessedBy(processedBy);
        setProcessedAt(processedAt);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void approve(String adminNote, BigDecimal approvedRefundAmount, UUID processedBy, Instant processedAt) {
        requirePending();
        setStatus(ReturnRequestStatus.APPROVED);
        setAdminNote(adminNote);
        setApprovedRefundAmount(approvedRefundAmount);
        setProcessedBy(Guard.notNull(
                processedBy,
                DomainErrorCode.INVALID_RETURN_REQUEST_PROCESSED_BY,
                "processedBy"
        ));
        setProcessedAt(processedAt == null ? Instant.now() : processedAt);
        setUpdatedAt(this.processedAt);
    }

    public void reject(String adminNote, UUID processedBy, Instant processedAt) {
        requirePending();
        this.adminNote = Guard.notBlank(
                adminNote,
                DomainErrorCode.INVALID_RETURN_REQUEST_ADMIN_NOTE,
                "adminNote"
        );
        setStatus(ReturnRequestStatus.REJECTED);
        setApprovedRefundAmount(null);
        setProcessedBy(Guard.notNull(
                processedBy,
                DomainErrorCode.INVALID_RETURN_REQUEST_PROCESSED_BY,
                "processedBy"
        ));
        setProcessedAt(processedAt == null ? Instant.now() : processedAt);
        setUpdatedAt(this.processedAt);
    }

    public void cancel() {
        requirePending();
        setStatus(ReturnRequestStatus.CANCELLED);
        setUpdatedAt(Instant.now());
    }

    private void requirePending() {
        if (status != ReturnRequestStatus.PENDING) {
            throw new DomainException(DomainErrorCode.RETURN_REQUEST_NOT_PENDING);
        }
    }

    private void setOrderId(UUID orderId) {
        this.orderId = Guard.notNull(
                orderId,
                DomainErrorCode.INVALID_RETURN_REQUEST_ORDER_ID,
                "orderId"
        );
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(
                userId,
                DomainErrorCode.INVALID_RETURN_REQUEST_USER_ID,
                "userId"
        );
    }

    private void setReason(String reason) {
        this.reason = Guard.notBlank(
                reason,
                DomainErrorCode.INVALID_RETURN_REQUEST_REASON,
                "reason"
        );
    }

    private void setStatus(ReturnRequestStatus status) {
        this.status = Guard.notNull(
                status,
                DomainErrorCode.INVALID_RETURN_REQUEST_STATUS,
                "status"
        );
    }

    private void setAdminNote(String adminNote) {
        this.adminNote = Guard.notBlankOrNull(
                adminNote,
                DomainErrorCode.INVALID_RETURN_REQUEST_ADMIN_NOTE,
                "adminNote"
        );
    }

    private void setRequestedRefundAmount(BigDecimal requestedRefundAmount) {
        if (requestedRefundAmount != null && requestedRefundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_RETURN_REQUEST_REQUESTED_REFUND_AMOUNT,
                    "requestedRefundAmount"
            );
        }
        this.requestedRefundAmount = requestedRefundAmount;
    }

    private void setApprovedRefundAmount(BigDecimal approvedRefundAmount) {
        if (approvedRefundAmount != null && approvedRefundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_RETURN_REQUEST_APPROVED_REFUND_AMOUNT,
                    "approvedRefundAmount"
            );
        }
        this.approvedRefundAmount = approvedRefundAmount;
    }

    private void setProcessedBy(UUID processedBy) {
        this.processedBy = processedBy;
    }

    private void setProcessedAt(Instant processedAt) {
        this.processedAt = Guard.notInFutureOrNull(
                processedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_PROCESSED_AT,
                "processedAt"
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_CREATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_UPDATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_DELETED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_AUDIT_ORDER
        );
        Guard.notBefore(
                this.processedAt,
                validCreatedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_AUDIT_ORDER,
                "processedAt",
                "createdAt"
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_CREATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_UPDATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_DELETED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_AUDIT_ORDER
        );
        Guard.notBefore(
                this.processedAt,
                this.createdAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_AUDIT_ORDER,
                "processedAt",
                "createdAt"
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_RETURN_REQUEST_CREATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_UPDATED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_DELETED_AT,
                DomainErrorCode.INVALID_RETURN_REQUEST_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
