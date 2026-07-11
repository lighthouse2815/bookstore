package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.model.PaymentReconciliationIssue;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IPaymentReconciliationIssueRepository {
    boolean existsByDeduplicationKey(String deduplicationKey);
    Optional<PaymentReconciliationIssue> findById(UUID id);
    PaymentReconciliationIssue save(PaymentReconciliationIssue issue);
    PageSliceResult<PaymentReconciliationIssue> findPage(
            int page, int size, PaymentReconciliationStatus status, PaymentReconciliationIssueType issueType, Instant from, Instant to
    );
}
