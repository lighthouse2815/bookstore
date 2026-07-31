package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class PaymentReconciliationIssueRule {

    private PaymentReconciliationIssueRule() {
    }

    public static void requireNonNegativeAmount(BigDecimal amount, String field) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_PAYMENT_RECONCILIATION_AMOUNT, field);
        }
    }

    public static void requireOpen(PaymentReconciliationStatus status) {
        if (status != PaymentReconciliationStatus.OPEN) {
            throw new DomainException(DomainErrorCode.PAYMENT_RECONCILIATION_ISSUE_NOT_OPEN);
        }
    }
}
