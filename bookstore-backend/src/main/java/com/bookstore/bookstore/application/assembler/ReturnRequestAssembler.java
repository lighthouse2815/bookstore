package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class ReturnRequestAssembler {

    public ReturnRequestResult toResult(
            ReturnRequest returnRequest,
            Order order,
            User user,
            User processedByUser
    ) {
        return new ReturnRequestResult(
                returnRequest.getId(),
                returnRequest.getOrderId(),
                order == null ? null : order.getOrderCode(),
                returnRequest.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                order == null ? null : order.getReceiverName(),
                returnRequest.getReason(),
                returnRequest.getStatus(),
                returnRequest.getRequestedRefundAmount(),
                returnRequest.getApprovedRefundAmount(),
                returnRequest.getAdminNote(),
                returnRequest.getProcessedBy(),
                processedByUser == null ? null : processedByUser.getUsername(),
                returnRequest.getProcessedAt(),
                order == null ? null : order.getStatus(),
                order == null ? null : order.getPaymentMethod(),
                order == null ? null : order.getPaymentStatus(),
                order == null ? null : order.getFinalAmount(),
                order == null ? null : order.getCreatedAt(),
                returnRequest.getCreatedAt(),
                returnRequest.getUpdatedAt()
        );
    }
}
