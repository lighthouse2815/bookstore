package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_reconciliation_issues", indexes = {
        @Index(name = "idx_payment_reconciliation_status_detected", columnList = "status,detected_at"),
        @Index(name = "idx_payment_reconciliation_payment_id", columnList = "payment_id"),
        @Index(name = "idx_payment_reconciliation_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentReconciliationIssueJpaEntity {
    @Id private UUID id;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Enumerated(EnumType.STRING) @Column(name = "issue_type", nullable = false, length = 64) private PaymentReconciliationIssueType issueType;
    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 2) private BigDecimal expectedAmount;
    @Column(name = "received_amount", nullable = false, precision = 19, scale = 2) private BigDecimal receivedAmount;
    @Column(name = "external_transaction_id", length = 100) private String externalTransactionId;
    @Column(name = "deduplication_key", nullable = false, unique = true, length = 64) private String deduplicationKey;
    @Column(columnDefinition = "TEXT") private String details;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private PaymentReconciliationStatus status;
    @Column(name = "detected_at", nullable = false) private Instant detectedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolved_by") private UUID resolvedBy;
    @Column(name = "resolution_note", length = 1000) private String resolutionNote;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
