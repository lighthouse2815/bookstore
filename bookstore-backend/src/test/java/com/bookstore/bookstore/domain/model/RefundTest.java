package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefundTest {
    @Test
    void success_requiresProcessingReferenceAndEvidence() {
        Refund refund = refund();
        refund.approve(UUID.randomUUID(), Instant.now());
        refund.startProcessing(UUID.randomUUID(), Instant.now());
        DomainException exception = assertThrows(
                DomainException.class,
                () -> refund.succeed(UUID.randomUUID(), "REF-1", null, null, Instant.now())
        );
        assertEquals(DomainErrorCode.REFUND_EVIDENCE_REQUIRED, exception.getErrorCode());
        refund.succeed(UUID.randomUUID(), "REF-1", "https://evidence.example/ref-1", null, Instant.now());
        assertEquals(RefundStatus.SUCCEEDED, refund.getStatus());
    }

    @Test
    void failedRefund_canRetryButTerminalRefundCannotMove() {
        Refund refund = refund();
        refund.approve(UUID.randomUUID(), Instant.now());
        refund.startProcessing(UUID.randomUUID(), Instant.now());
        refund.fail(UUID.randomUUID(), "Ngân hàng chưa xác nhận", Instant.now());
        refund.startProcessing(UUID.randomUUID(), Instant.now());
        assertEquals(RefundStatus.PROCESSING, refund.getStatus());
        refund.succeed(UUID.randomUUID(), "REF-2", null, "bank slip", Instant.now());
        DomainException exception = assertThrows(
                DomainException.class,
                () -> refund.cancel(UUID.randomUUID(), null, Instant.now())
        );
        assertEquals(DomainErrorCode.INVALID_REFUND_STATUS_TRANSITION, exception.getErrorCode());
    }

    @Test
    void requestedRefund_cannotSkipApproval() {
        Refund refund = refund();
        DomainException exception = assertThrows(
                DomainException.class,
                () -> refund.startProcessing(UUID.randomUUID(), Instant.now())
        );
        assertEquals(DomainErrorCode.INVALID_REFUND_STATUS_TRANSITION, exception.getErrorCode());
    }

    private static Refund refund() {
        Instant now = Instant.EPOCH;
        return new Refund(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, new BigDecimal("10.00"), "VND", "Khách hủy đơn", RefundMethod.MANUAL_BANK_TRANSFER,
                RefundStatus.REQUESTED, null, null, null, UUID.randomUUID().toString(), UUID.randomUUID(), null, null, now, null, null, null, now, now, 0);
    }
}
