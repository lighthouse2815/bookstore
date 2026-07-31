package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;

public final class PaymentRule {

    private PaymentRule() {
    }

    public static void requireNonNegativeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_PAYMENT_AMOUNT, "amount");
        }
    }

    public static void requireCanTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        boolean allowed = currentStatus == PaymentStatus.PENDING
                && (targetStatus == PaymentStatus.PAID
                || targetStatus == PaymentStatus.CANCELLED
                || targetStatus == PaymentStatus.EXPIRED);
        if (!allowed) {
            throw new DomainException(
                    DomainErrorCode.INVALID_PAYMENT_STATUS_TRANSITION,
                    currentStatus,
                    targetStatus
            );
        }
    }
}
