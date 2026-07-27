package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.PaymentReconciliationIssueResult;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface IPaymentReconciliationService {
    void recordIssue(Payment payment, Order order, PaymentReconciliationIssueType type, BigDecimal receivedAmount, String externalTransactionId, String details);
    PageSliceResult<PaymentReconciliationIssueResult> getPage(PageQuery pageQuery, PaymentReconciliationStatus status, PaymentReconciliationIssueType type, Instant from, Instant to);
    PaymentReconciliationIssueResult getById(UUID id);
    PaymentReconciliationIssueResult resolve(UUID id, UUID resolverId, String resolutionNote);
}
