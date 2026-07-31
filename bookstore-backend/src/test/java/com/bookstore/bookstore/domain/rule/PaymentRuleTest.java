package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class PaymentRuleTest {

    @Test
    void requireCanTransition_rejectsChangingTerminalPayment() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> PaymentRule.requireCanTransition(PaymentStatus.PAID, PaymentStatus.CANCELLED)
        );

        assertEquals(DomainErrorCode.INVALID_PAYMENT_STATUS_TRANSITION, exception.getErrorCode());
    }
}
