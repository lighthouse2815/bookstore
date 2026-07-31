package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class RefundRule {

    private RefundRule() {
    }

    public static void requirePositiveAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_REFUND_AMOUNT, "amount");
        }
    }

    public static void requireCanTransition(RefundStatus currentStatus, RefundStatus targetStatus) {
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new DomainException(
                    DomainErrorCode.INVALID_REFUND_STATUS_TRANSITION,
                    currentStatus,
                    targetStatus
            );
        }
    }

    public static void requireEvidence(String evidenceUrl, String evidenceMetadata) {
        if (evidenceUrl == null && evidenceMetadata == null) {
            throw new DomainException(DomainErrorCode.REFUND_EVIDENCE_REQUIRED);
        }
    }
}
