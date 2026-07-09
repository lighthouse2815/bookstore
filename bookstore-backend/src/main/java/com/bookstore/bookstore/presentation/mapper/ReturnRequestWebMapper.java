package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.ApproveReturnRequestCommand;
import com.bookstore.bookstore.application.command.CancelReturnRequestCommand;
import com.bookstore.bookstore.application.command.CreateReturnRequestCommand;
import com.bookstore.bookstore.application.command.RejectReturnRequestCommand;
import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.presentation.request.ApproveReturnRequestRequest;
import com.bookstore.bookstore.presentation.request.CreateReturnRequestRequest;
import com.bookstore.bookstore.presentation.request.RejectReturnRequestRequest;
import com.bookstore.bookstore.presentation.response.ReturnRequestResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReturnRequestWebMapper {

    public CreateReturnRequestCommand toCreateCommand(
            UUID orderId,
            UUID userId,
            CreateReturnRequestRequest request
    ) {
        return new CreateReturnRequestCommand(
                orderId,
                userId,
                request.reason(),
                request.requestedRefundAmount()
        );
    }

    public ApproveReturnRequestCommand toApproveCommand(
            UUID requestId,
            UUID adminUserId,
            ApproveReturnRequestRequest request
    ) {
        return new ApproveReturnRequestCommand(
                requestId,
                adminUserId,
                request.adminNote(),
                request.approvedRefundAmount(),
                request.restock()
        );
    }

    public RejectReturnRequestCommand toRejectCommand(
            UUID requestId,
            UUID adminUserId,
            RejectReturnRequestRequest request
    ) {
        return new RejectReturnRequestCommand(
                requestId,
                adminUserId,
                request.adminNote()
        );
    }

    public CancelReturnRequestCommand toCancelCommand(UUID requestId, UUID userId) {
        return new CancelReturnRequestCommand(requestId, userId);
    }

    public ReturnRequestResponse toResponse(ReturnRequestResult result) {
        return new ReturnRequestResponse(
                result.id(),
                result.orderId(),
                result.orderCode(),
                result.userId(),
                result.username(),
                result.userEmail(),
                result.receiverName(),
                result.reason(),
                result.status(),
                result.requestedRefundAmount(),
                result.approvedRefundAmount(),
                result.adminNote(),
                result.processedBy(),
                result.processedByName(),
                result.processedAt(),
                result.orderStatus(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.orderFinalAmount(),
                result.orderCreatedAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
