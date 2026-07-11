package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IPaymentReconciliationIssueRepository;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.model.PaymentReconciliationIssue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

    @Mock private IPaymentReconciliationIssueRepository issueRepository;
    @InjectMocks private PaymentReconciliationService service;

    @Test
    void resolve_requiresNonBlankResolutionNote() {
        PaymentReconciliationIssue issue = openIssue();
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.resolve(issue.getId(), UUID.randomUUID(), "  ")
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void resolve_closedIssue_returnsStableConflictCode() {
        PaymentReconciliationIssue issue = new PaymentReconciliationIssue(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                PaymentReconciliationIssueType.AMOUNT_MISMATCH, BigDecimal.TEN, BigDecimal.ONE,
                "TXN", "dedup", null, PaymentReconciliationStatus.RESOLVED, Instant.now(),
                Instant.now(), UUID.randomUUID(), "Đã xử lý", Instant.now(), Instant.now()
        );
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.resolve(issue.getId(), UUID.randomUUID(), "Thử xử lý lại")
        );

        assertEquals(ApplicationErrorCode.PAYMENT_RECONCILIATION_NOT_OPEN, exception.getErrorCode());
    }

    private static PaymentReconciliationIssue openIssue() {
        Instant now = Instant.now();
        return new PaymentReconciliationIssue(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                PaymentReconciliationIssueType.PAYMENT_AFTER_EXPIRY, BigDecimal.TEN, BigDecimal.TEN,
                "TXN", "dedup", "Late payment", PaymentReconciliationStatus.OPEN, now,
                null, null, null, now, now
        );
    }
}
