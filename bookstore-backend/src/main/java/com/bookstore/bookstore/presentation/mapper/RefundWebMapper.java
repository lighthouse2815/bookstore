package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CancelRefundCommand;
import com.bookstore.bookstore.application.command.CreateRefundCommand;
import com.bookstore.bookstore.application.command.FailRefundCommand;
import com.bookstore.bookstore.application.command.SucceedRefundCommand;
import com.bookstore.bookstore.application.result.RefundResult;
import com.bookstore.bookstore.presentation.request.CancelRefundRequest;
import com.bookstore.bookstore.presentation.request.CreateRefundRequest;
import com.bookstore.bookstore.presentation.request.FailRefundRequest;
import com.bookstore.bookstore.presentation.request.SucceedRefundRequest;
import com.bookstore.bookstore.presentation.response.RefundResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RefundWebMapper {
    public CreateRefundCommand toCreate(UUID orderId, UUID requestedBy, String idempotencyKey, CreateRefundRequest request) {
        return new CreateRefundCommand(orderId, request.returnRequestId(), request.amount(), request.currency(), request.reason(), request.method(), idempotencyKey, requestedBy);
    }
    public SucceedRefundCommand toSucceed(UUID id, UUID processedBy, SucceedRefundRequest request) {
        return new SucceedRefundCommand(id, processedBy, request.externalReference(), request.evidenceUrl(), request.evidenceMetadata());
    }
    public FailRefundCommand toFail(UUID id, UUID processedBy, FailRefundRequest request) {
        return new FailRefundCommand(id, processedBy, request.failureReason());
    }
    public CancelRefundCommand toCancel(UUID id, UUID processedBy, CancelRefundRequest request) {
        return new CancelRefundCommand(id, processedBy, request == null ? null : request.reason());
    }
    public RefundResponse toResponse(RefundResult result) {
        return new RefundResponse(result.id(), result.orderId(), result.orderCode(), result.paymentId(), result.paymentProvider(), result.paymentStatus(), result.paidAmount(),
                result.returnRequestId(), result.amount(), result.currency(), result.reason(), result.method(), result.status(), result.externalReference(), result.evidenceUrl(),
                result.evidenceMetadata(), result.requestedBy(), result.approvedBy(), result.processedBy(), result.requestedAt(), result.approvedAt(), result.processedAt(),
                result.failureReason(), result.createdAt(), result.updatedAt());
    }
}
