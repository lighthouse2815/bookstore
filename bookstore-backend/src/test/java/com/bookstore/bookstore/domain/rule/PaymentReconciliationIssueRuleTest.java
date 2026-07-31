package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class PaymentReconciliationIssueRuleTest {

    @Test
    void requireOpen_rejectsResolvedIssue() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> PaymentReconciliationIssueRule.requireOpen(PaymentReconciliationStatus.RESOLVED)
        );

        assertEquals(
                DomainErrorCode.PAYMENT_RECONCILIATION_ISSUE_NOT_OPEN,
                exception.getErrorCode()
        );
    }
}
