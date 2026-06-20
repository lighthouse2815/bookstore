package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreatePosOrderCommand;
import com.bookstore.bookstore.application.command.CreatePosOrderItemCommand;
import com.bookstore.bookstore.application.result.CreatePosOrderResult;
import com.bookstore.bookstore.presentation.request.CreatePosOrderItemRequest;
import com.bookstore.bookstore.presentation.request.CreatePosOrderRequest;
import com.bookstore.bookstore.presentation.response.CreatePosOrderResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PosOrderWebMapper {

    public CreatePosOrderCommand toCommand(UUID staffUserId, CreatePosOrderRequest request) {
        return new CreatePosOrderCommand(
                staffUserId,
                request.customerName(),
                request.customerPhone(),
                request.paymentMethod(),
                request.couponCode(),
                request.items().stream()
                        .map(this::toItemCommand)
                        .toList()
        );
    }

    public CreatePosOrderResponse toResponse(CreatePosOrderResult result) {
        return new CreatePosOrderResponse(
                result.orderId(),
                result.orderCode(),
                result.totalAmount(),
                result.discountAmount(),
                result.finalAmount(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.orderStatus()
        );
    }

    private CreatePosOrderItemCommand toItemCommand(CreatePosOrderItemRequest request) {
        return new CreatePosOrderItemCommand(request.bookId(), request.quantity());
    }
}
