package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.PaymentRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Payment {

    private UUID id;
    private UUID orderId;
    private PaymentProvider provider;
    private PaymentStatus status;
    private BigDecimal amount;
    private String merchantId;
    private String transactionId;
    private String referenceCode;
    private String transferContent;
    private String gateway;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private Instant expiredAt;

    public Payment(
            UUID id,
            UUID orderId,
            PaymentProvider provider,
            PaymentStatus status,
            BigDecimal amount,
            String merchantId,
            String transactionId,
            String referenceCode,
            String transferContent,
            String gateway,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                orderId,
                provider,
                status,
                amount,
                merchantId,
                transactionId,
                referenceCode,
                transferContent,
                gateway,
                paidAt,
                createdAt,
                updatedAt,
                null,
                null
        );
    }

    public Payment(
            UUID id,
            UUID orderId,
            PaymentProvider provider,
            PaymentStatus status,
            BigDecimal amount,
            String merchantId,
            String transactionId,
            String referenceCode,
            String transferContent,
            String gateway,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt,
            Instant expiredAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PAYMENT_ID, "id");
        setOrderId(orderId);
        setProvider(provider);
        setStatus(status);
        setAmount(amount);
        setMerchantId(merchantId);
        setTransactionId(transactionId);
        setReferenceCode(referenceCode);
        setTransferContent(transferContent);
        setGateway(gateway);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setPaidAt(paidAt);
        setExpiresAt(expiresAt);
        setExpiredAt(expiredAt);
    }

    public void markPaid(
            String merchantId,
            String transactionId,
            String referenceCode,
            String gateway,
            Instant paidAt
    ) {
        PaymentRule.requireCanTransition(status, PaymentStatus.PAID);
        if (merchantId != null) {
            setMerchantId(merchantId);
        }
        if (transactionId != null) {
            setTransactionId(transactionId);
        }
        if (referenceCode != null) {
            setReferenceCode(referenceCode);
        }
        if (gateway != null) {
            setGateway(gateway);
        }
        setStatus(PaymentStatus.PAID);
        setPaidAt(paidAt);
        setUpdatedAt(paidAt);
    }

    public void markCancelled(Instant cancelledAt) {
        PaymentRule.requireCanTransition(status, PaymentStatus.CANCELLED);
        setStatus(PaymentStatus.CANCELLED);
        setUpdatedAt(cancelledAt);
    }

    public void markExpired(Instant expiredAt) {
        PaymentRule.requireCanTransition(status, PaymentStatus.EXPIRED);
        setStatus(PaymentStatus.EXPIRED);
        setExpiredAt(expiredAt);
        setUpdatedAt(expiredAt);
    }

    /**
     * Keeps the money-transfer reference for manual reconciliation without changing the payment state.
     * In particular, this must not revive an expired or cancelled payment.
     */
    public void recordIncomingTransfer(
            String merchantId,
            String transactionId,
            String referenceCode,
            String gateway,
            Instant receivedAt
    ) {
        if (merchantId != null) {
            setMerchantId(merchantId);
        }
        if (transactionId != null) {
            setTransactionId(transactionId);
        }
        if (referenceCode != null) {
            setReferenceCode(referenceCode);
        }
        if (gateway != null) {
            setGateway(gateway);
        }
        setUpdatedAt(receivedAt);
    }

    private void setOrderId(UUID orderId) {
        this.orderId = Guard.notNull(orderId, DomainErrorCode.INVALID_PAYMENT_ORDER_ID, "orderId");
    }

    private void setProvider(PaymentProvider provider) {
        this.provider = Guard.notNull(provider, DomainErrorCode.INVALID_PAYMENT_PROVIDER, "provider");
    }

    private void setStatus(PaymentStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_PAYMENT_STATUS, "status");
    }

    private void setAmount(BigDecimal amount) {
        BigDecimal validAmount = Guard.notNull(amount, DomainErrorCode.INVALID_PAYMENT_AMOUNT, "amount");
        PaymentRule.requireNonNegativeAmount(validAmount);
        this.amount = validAmount;
    }

    private void setMerchantId(String merchantId) {
        this.merchantId = Guard.notBlankOrNull(
                merchantId,
                DomainErrorCode.INVALID_PAYMENT_MERCHANT_ID,
                "merchantId"
        );
    }

    private void setTransactionId(String transactionId) {
        this.transactionId = Guard.notBlankOrNull(
                transactionId,
                DomainErrorCode.INVALID_PAYMENT_TRANSACTION_ID,
                "transactionId"
        );
    }

    private void setReferenceCode(String referenceCode) {
        this.referenceCode = Guard.notBlank(
                referenceCode,
                DomainErrorCode.INVALID_PAYMENT_REFERENCE_CODE,
                "referenceCode"
        );
    }

    private void setTransferContent(String transferContent) {
        this.transferContent = Guard.notBlank(
                transferContent,
                DomainErrorCode.INVALID_PAYMENT_TRANSFER_CONTENT,
                "transferContent"
        );
    }

    private void setGateway(String gateway) {
        this.gateway = Guard.notBlankOrNull(
                gateway,
                DomainErrorCode.INVALID_PAYMENT_GATEWAY,
                "gateway"
        );
    }

    private void setPaidAt(Instant paidAt) {
        Instant validPaidAt = Guard.notInFutureOrNull(
                paidAt,
                DomainErrorCode.INVALID_PAYMENT_PAID_AT,
                "paidAt"
        );
        Guard.notBefore(
                validPaidAt,
                this.createdAt,
                DomainErrorCode.INVALID_PAYMENT_AUDIT_ORDER,
                "paidAt",
                "createdAt"
        );
        this.paidAt = validPaidAt;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_PAYMENT_CREATED_AT,
                "createdAt"
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFuture(
                updatedAt,
                DomainErrorCode.INVALID_PAYMENT_UPDATED_AT,
                "updatedAt"
        );
        Guard.notBefore(
                validUpdatedAt,
                this.createdAt,
                DomainErrorCode.INVALID_PAYMENT_AUDIT_ORDER,
                "updatedAt",
                "createdAt"
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setExpiresAt(Instant expiresAt) {
        if (expiresAt != null) {
            Guard.notBefore(
                    expiresAt,
                    this.createdAt,
                    DomainErrorCode.INVALID_PAYMENT_EXPIRES_AT,
                    "expiresAt",
                    "createdAt"
            );
        }
        this.expiresAt = expiresAt;
    }

    private void setExpiredAt(Instant expiredAt) {
        Instant validExpiredAt = Guard.notInFutureOrNull(
                expiredAt,
                DomainErrorCode.INVALID_PAYMENT_EXPIRED_AT,
                "expiredAt"
        );
        Guard.notBefore(
                validExpiredAt,
                this.createdAt,
                DomainErrorCode.INVALID_PAYMENT_AUDIT_ORDER,
                "expiredAt",
                "createdAt"
        );
        this.expiredAt = validExpiredAt;
    }
}
