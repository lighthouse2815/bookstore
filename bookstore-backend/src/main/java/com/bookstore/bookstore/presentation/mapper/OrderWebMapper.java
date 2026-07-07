package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateOrderCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.result.CreateOrderResult;
import com.bookstore.bookstore.application.result.OrderItemResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.presentation.request.CreateOrderRequest;
import com.bookstore.bookstore.presentation.request.UpdateOrderStatusRequest;
import com.bookstore.bookstore.presentation.response.CreateOrderResponse;
import com.bookstore.bookstore.presentation.response.OrderItemResponse;
import com.bookstore.bookstore.presentation.response.OrderResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderWebMapper {

    public CreateOrderCommand toCreateOrderCommand(UUID userId, CreateOrderRequest request) {
        return new CreateOrderCommand(
                userId,
                request.cartItemIds(),
                request.addressId(),
                request.shippingMethod(),
                request.paymentMethod(),
                request.bookCouponCode(),
                request.shippingCouponCode(),
                request.note()
        );
    }

    public UpdateOrderStatusCommand toUpdateStatusCommand(UUID orderId, UpdateOrderStatusRequest request) {
        return new UpdateOrderStatusCommand(orderId, request.status());
    }

    public CreateOrderResponse toCreateOrderResponse(CreateOrderResult result) {
        return new CreateOrderResponse(
                result.orderId(),
                result.orderCode(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.totalAmount(),
                result.transferContent()
        );
    }

    public OrderResponse toResponse(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.orderCode(),
                result.userId(),
                result.items().stream()
                        .map(this::toItemResponse)
                        .toList(),
                result.productTotal(),
                result.totalAmount(),
                result.discountAmount(),
                result.shippingFee(),
                result.shippingDiscount(),
                result.couponDiscount(),
                result.finalAmount(),
                result.couponId(),
                result.couponCode(),
                result.bookCouponId(),
                result.bookCouponCode(),
                result.shippingCouponId(),
                result.shippingCouponCode(),
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
                result.itemType(),
                result.bookId(),
                result.digitalAssetId(),
                result.bookTitle(),
                result.unitPrice(),
                result.quantity(),
                result.lineTotal()
        );
    }
}
