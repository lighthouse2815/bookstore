package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CheckoutCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.result.OrderItemResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.presentation.request.CheckoutRequest;
import com.bookstore.bookstore.presentation.request.UpdateOrderStatusRequest;
import com.bookstore.bookstore.presentation.response.OrderItemResponse;
import com.bookstore.bookstore.presentation.response.OrderResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderWebMapper {

    public CheckoutCommand toCheckoutCommand(UUID userId, CheckoutRequest request) {
        return new CheckoutCommand(
                userId,
                request.addressId(),
                request.couponCode()
        );
    }

    public UpdateOrderStatusCommand toUpdateStatusCommand(UUID orderId, UpdateOrderStatusRequest request) {
        return new UpdateOrderStatusCommand(orderId, request.status());
    }

    public OrderResponse toResponse(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.userId(),
                result.items().stream()
                        .map(this::toItemResponse)
                        .toList(),
                result.totalAmount(),
                result.discountAmount(),
                result.shippingFee(),
                result.finalAmount(),
                result.couponId(),
                result.couponCode(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.status(),
                result.receiverName(),
                result.receiverPhone(),
                result.receiverAddress(),
                result.createdAt(),
                result.updatedAt(),
                result.cancelledAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItemResult result) {
        return new OrderItemResponse(
                result.id(),
                result.bookId(),
                result.bookTitle(),
                result.unitPrice(),
                result.quantity(),
                result.lineTotal()
        );
    }
}
