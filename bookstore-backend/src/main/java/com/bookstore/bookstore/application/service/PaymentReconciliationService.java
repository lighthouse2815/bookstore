package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPaymentReconciliationService;
import com.bookstore.bookstore.application.port.out.IPaymentReconciliationIssueRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.PaymentReconciliationIssueResult;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.PaymentReconciliationIssue;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentReconciliationService implements IPaymentReconciliationService {
    private final IPaymentReconciliationIssueRepository issueRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordIssue(Payment payment, Order order, PaymentReconciliationIssueType type, BigDecimal receivedAmount, String externalTransactionId, String details) {
        String deduplicationKey = digest(payment.getId() + "|" + type + "|" + StringUtils.trimToNull(externalTransactionId) + "|" + receivedAmount);
        if (issueRepository.existsByDeduplicationKey(deduplicationKey)) return;
        Instant now = Instant.now();
        issueRepository.save(new PaymentReconciliationIssue(
                UUID.randomUUID(), payment.getId(), order.getId(), type, payment.getAmount(),
                receivedAmount == null ? BigDecimal.ZERO : receivedAmount, externalTransactionId, deduplicationKey,
                details, PaymentReconciliationStatus.OPEN, now, null, null, null, now, now
        ));
    }

    @Override @Transactional(readOnly = true)
    public PageSliceResult<PaymentReconciliationIssueResult> getPage(int page, int size, PaymentReconciliationStatus status, PaymentReconciliationIssueType type, Instant from, Instant to) {
        if (page < 0 || size <= 0) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page/size");
        return issueRepository.findPage(page, size, status, type, from, to).map(this::toResult);
    }
    @Override @Transactional(readOnly = true)
    public PaymentReconciliationIssueResult getById(UUID id) { return toResult(find(id)); }
    @Override @Transactional(rollbackFor = Exception.class)
    public PaymentReconciliationIssueResult resolve(UUID id, UUID resolverId, String note) {
        PaymentReconciliationIssue issue = find(id);
        if (issue.getStatus() != PaymentReconciliationStatus.OPEN) throw new ApplicationException(ApplicationErrorCode.PAYMENT_RECONCILIATION_NOT_OPEN);
        String normalizedNote = StringUtils.trimToNull(note);
        if (normalizedNote == null || normalizedNote.length() > 1000) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "resolutionNote");
        issue.resolve(resolverId, normalizedNote, Instant.now());
        return toResult(issueRepository.save(issue));
    }
    private PaymentReconciliationIssue find(UUID id) { return issueRepository.findById(id).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_RECONCILIATION_NOT_FOUND)); }
    private PaymentReconciliationIssueResult toResult(PaymentReconciliationIssue issue) { return new PaymentReconciliationIssueResult(issue.getId(), issue.getPaymentId(), issue.getOrderId(), issue.getIssueType(), issue.getExpectedAmount(), issue.getReceivedAmount(), issue.getExternalTransactionId(), issue.getDetails(), issue.getStatus(), issue.getDetectedAt(), issue.getResolvedAt(), issue.getResolvedBy(), issue.getResolutionNote(), issue.getCreatedAt(), issue.getUpdatedAt()); }
    private String digest(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException("SHA-256 không khả dụng", e); } }
}
