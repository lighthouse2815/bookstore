package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.command.CancelRefundCommand;
import com.bookstore.bookstore.application.command.CreateRefundCommand;
import com.bookstore.bookstore.application.command.EnqueueOutboxEventCommand;
import com.bookstore.bookstore.application.command.FailRefundCommand;
import com.bookstore.bookstore.application.command.OutboxNotificationPayload;
import com.bookstore.bookstore.application.command.SucceedRefundCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.in.IRefundService;
import com.bookstore.bookstore.application.port.in.ITransactionalOutboxService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IRefundRepository;
import com.bookstore.bookstore.application.port.out.IReturnRequestRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.RefundResult;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.Refund;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService implements IRefundService {
    private static final List<RefundStatus> RESERVED_STATUSES = List.of(
            RefundStatus.REQUESTED, RefundStatus.APPROVED, RefundStatus.PROCESSING, RefundStatus.SUCCEEDED
    );
    private static final List<RefundStatus> SUCCEEDED_STATUS = List.of(RefundStatus.SUCCEEDED);

    private final IRefundRepository refundRepository;
    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;
    private final IReturnRequestRepository returnRequestRepository;
    private final IAuditLogService auditLogService;
    private final IOrderTimelineService orderTimelineService;
    private final ITransactionalOutboxService transactionalOutboxService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult create(CreateRefundCommand command) {
        if (command == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        validateCreate(command);
        Refund existing = refundRepository.findByOrderIdAndIdempotencyKey(command.orderId(), command.idempotencyKey()).orElse(null);
        if (existing != null) return toResult(existing);

        LockedContext context = lockContextByOrder(command.orderId());
        existing = refundRepository.findByOrderIdAndIdempotencyKey(command.orderId(), command.idempotencyKey()).orElse(null);
        if (existing != null) return toResult(existing);
        requirePaid(context);
        validateReturnRequest(command.returnRequestId(), context.order());
        assertWithinRemaining(context.payment(), command.amount(), RESERVED_STATUSES);
        Instant now = Instant.now();
        Refund refund = new Refund(UUID.randomUUID(), context.order().getId(), context.payment().getId(), command.returnRequestId(),
                command.amount(), normalizeCurrency(command.currency()), command.reason(), command.method(), RefundStatus.REQUESTED,
                null, null, null, command.idempotencyKey(), command.requestedBy(), null, null, now, null, null, null, now, now, 0);
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, null, command.requestedBy());
        return toResult(saved, context.order(), context.payment());
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResult getById(UUID id) { return toResult(find(id)); }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<RefundResult> getPage(PageQuery pageQuery, RefundStatus status, RefundMethod method, Instant from, Instant to) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "date");
        }
        return refundRepository.findPage(pageQuery.page(), pageQuery.size(), status, method, from, to)
                .map(this::toResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult approve(UUID id, UUID approvedBy) {
        LockedContext context = lockContextByRefund(id);
        Refund refund = context.refund();
        if (refund.getStatus() == RefundStatus.APPROVED) return toResult(refund, context.order(), context.payment());
        requireTransition(refund, RefundStatus.APPROVED);
        RefundStatus previous = refund.getStatus();
        refund.approve(requireUser(approvedBy), Instant.now());
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, previous, approvedBy);
        return toResult(saved, context.order(), context.payment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult startProcessing(UUID id, UUID processedBy) {
        LockedContext context = lockContextByRefund(id);
        Refund refund = context.refund();
        if (refund.getStatus() == RefundStatus.PROCESSING) return toResult(refund, context.order(), context.payment());
        requireTransition(refund, RefundStatus.PROCESSING);
        if (refund.getStatus() == RefundStatus.FAILED) assertWithinRemaining(context.payment(), refund.getAmount(), RESERVED_STATUSES);
        RefundStatus previous = refund.getStatus();
        refund.startProcessing(requireUser(processedBy), Instant.now());
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, previous, processedBy);
        return toResult(saved, context.order(), context.payment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult succeed(SucceedRefundCommand command) {
        if (command == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        if (StringUtils.trimToNull(command.externalReference()) == null
                || (StringUtils.trimToNull(command.evidenceUrl()) == null && StringUtils.trimToNull(command.evidenceMetadata()) == null)) {
            throw new ApplicationException(ApplicationErrorCode.REFUND_EVIDENCE_REQUIRED);
        }
        LockedContext context = lockContextByRefund(command.refundId());
        Refund refund = context.refund();
        if (refund.getStatus() == RefundStatus.SUCCEEDED) return toResult(refund, context.order(), context.payment());
        requireTransition(refund, RefundStatus.SUCCEEDED);
        assertWithinRemaining(context.payment(), refund.getAmount(), SUCCEEDED_STATUS);
        RefundStatus previous = refund.getStatus();
        refund.succeed(requireUser(command.processedBy()), command.externalReference(), command.evidenceUrl(), command.evidenceMetadata(), Instant.now());
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, previous, command.processedBy());
        return toResult(saved, context.order(), context.payment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult fail(FailRefundCommand command) {
        if (command == null || StringUtils.trimToNull(command.failureReason()) == null) {
            throw new ApplicationException(ApplicationErrorCode.REFUND_FAILURE_REASON_REQUIRED);
        }
        LockedContext context = lockContextByRefund(command.refundId());
        Refund refund = context.refund();
        if (refund.getStatus() == RefundStatus.FAILED) return toResult(refund, context.order(), context.payment());
        requireTransition(refund, RefundStatus.FAILED);
        RefundStatus previous = refund.getStatus();
        refund.fail(requireUser(command.processedBy()), command.failureReason(), Instant.now());
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, previous, command.processedBy());
        return toResult(saved, context.order(), context.payment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult cancel(CancelRefundCommand command) {
        if (command == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        LockedContext context = lockContextByRefund(command.refundId());
        Refund refund = context.refund();
        if (refund.getStatus() == RefundStatus.CANCELLED) return toResult(refund, context.order(), context.payment());
        requireTransition(refund, RefundStatus.CANCELLED);
        RefundStatus previous = refund.getStatus();
        refund.cancel(requireUser(command.processedBy()), command.reason(), Instant.now());
        Refund saved = refundRepository.save(refund);
        recordChange(context.order(), saved, previous, command.processedBy());
        return toResult(saved, context.order(), context.payment());
    }

    private LockedContext lockContextByOrder(UUID orderId) {
        if (orderId == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        Payment payment = paymentRepository.findByOrderIdForUpdate(orderId).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        return new LockedContext(order, payment, null);
    }

    private LockedContext lockContextByRefund(UUID refundId) {
        Refund preview = find(refundId);
        Payment payment = paymentRepository.findByIdForUpdate(preview.getPaymentId()).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        Order order = orderRepository.findByIdForUpdate(preview.getOrderId()).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        Refund refund = refundRepository.findByIdForUpdate(refundId).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REFUND_NOT_FOUND));
        return new LockedContext(order, payment, refund);
    }

    private void requirePaid(LockedContext context) {
        if (context.payment().getStatus() != PaymentStatus.PAID || context.order().getPaymentStatus() != PaymentStatus.PAID) {
            throw new ApplicationException(ApplicationErrorCode.REFUND_ORDER_NOT_PAID);
        }
    }
    private void validateReturnRequest(UUID returnRequestId, Order order) {
        if (returnRequestId == null) return;
        ReturnRequest request = returnRequestRepository.findByIdActive(returnRequestId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        if (!order.getId().equals(request.getOrderId()) || request.getStatus() != ReturnRequestStatus.APPROVED) {
            throw new ApplicationException(ApplicationErrorCode.REFUND_RETURN_REQUEST_INVALID);
        }
    }
    private void assertWithinRemaining(Payment payment, BigDecimal amount, List<RefundStatus> statuses) {
        BigDecimal committed = refundRepository.sumAmountByPaymentIdAndStatuses(payment.getId(), statuses);
        if (committed.add(amount).compareTo(payment.getAmount()) > 0) {
            throw new ApplicationException(ApplicationErrorCode.REFUND_AMOUNT_EXCEEDS_REMAINING);
        }
    }
    private void requireTransition(Refund refund, RefundStatus target) {
        if (!refund.getStatus().canTransitionTo(target)) throw new ApplicationException(ApplicationErrorCode.REFUND_INVALID_TRANSITION);
    }
    private Refund find(UUID id) {
        if (id == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "refundId");
        return refundRepository.findById(id).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REFUND_NOT_FOUND));
    }
    private void validateCreate(CreateRefundCommand command) {
        if (command.orderId() == null || command.requestedBy() == null || command.amount() == null || command.amount().signum() <= 0
                || StringUtils.trimToNull(command.reason()) == null || command.method() == null || StringUtils.trimToNull(command.idempotencyKey()) == null
                || command.idempotencyKey().trim().length() > 64) throw new ApplicationException(ApplicationErrorCode.REFUND_AMOUNT_INVALID);
    }
    private UUID requireUser(UUID userId) {
        if (userId == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "adminUserId");
        return userId;
    }
    private String normalizeCurrency(String currency) {
        String normalized = StringUtils.trimToNull(currency);
        if (normalized == null) normalized = "VND";
        normalized = normalized.toUpperCase(java.util.Locale.ROOT);
        if (!"VND".equals(normalized)) throw new ApplicationException(ApplicationErrorCode.REFUND_CURRENCY_INVALID);
        return normalized;
    }
    private void recordChange(Order order, Refund refund, RefundStatus previous, UUID actorId) {
        orderTimelineService.recordRefundStateChanged(order, refund, previous);
        Map<String, Object> before = new LinkedHashMap<>();
        if (previous != null) before.put("status", previous.name());
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", refund.getStatus().name());
        after.put("amount", refund.getAmount());
        auditLogService.recordStatusChange(new AuditLogCommand(actorId, null, "ADMIN", "REFUND_" + refund.getStatus(), AuditTargetType.REFUND,
                refund.getId().toString(), "Cập nhật hoàn tiền cho đơn " + order.getOrderCode(),
                before, after, null, null, Instant.now()));
        transactionalOutboxService.enqueue(new EnqueueOutboxEventCommand("REFUND", refund.getId(), "REFUND_" + refund.getStatus(),
                new OutboxNotificationPayload(order.getUserId(), "Cập nhật hoàn tiền", "Yêu cầu hoàn tiền cho đơn " + order.getOrderCode() + " đang ở trạng thái " + refund.getStatus(),
                        "REFUND", "REFUND", refund.getId(), "/orders/" + order.getId()),
                refund.getId() + "|notification|" + refund.getStatus()));
    }
    private RefundResult toResult(Refund refund) {
        Order order = orderRepository.findById(refund.getOrderId()).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        Payment payment = paymentRepository.findById(refund.getPaymentId()).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        return toResult(refund, order, payment);
    }
    private RefundResult toResult(Refund refund, Order order, Payment payment) {
        return new RefundResult(refund.getId(), order.getId(), order.getOrderCode(), payment.getId(), payment.getProvider(), payment.getStatus(), payment.getAmount(),
                refund.getReturnRequestId(), refund.getAmount(), refund.getCurrency(), refund.getReason(), refund.getMethod(), refund.getStatus(), refund.getExternalReference(),
                refund.getEvidenceUrl(), refund.getEvidenceMetadata(), refund.getRequestedBy(), refund.getApprovedBy(), refund.getProcessedBy(), refund.getRequestedAt(),
                refund.getApprovedAt(), refund.getProcessedAt(), refund.getFailureReason(), refund.getCreatedAt(), refund.getUpdatedAt());
    }
    private record LockedContext(Order order, Payment payment, Refund refund) { }
}
