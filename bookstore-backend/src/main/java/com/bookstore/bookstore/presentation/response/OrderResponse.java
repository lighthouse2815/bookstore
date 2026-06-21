package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String orderCode,
        UUID userId,
        List<OrderItemResponse> items,
        BigDecimal productTotal,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal shippingDiscount,
        BigDecimal couponDiscount,
        BigDecimal finalAmount,
        UUID couponId,
        String couponCode,
        UUID bookCouponId,
        String bookCouponCode,
        UUID shippingCouponId,
        String shippingCouponCode,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus status,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        Instant createdAt,
        Instant updatedAt,
        Instant cancelledAt
) {
    public OrderResponse(
            UUID orderId,
            UUID userId,
            List<OrderItemResponse> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount,
            UUID couponId,
            String couponCode,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus status,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            Instant createdAt,
            Instant updatedAt,
            Instant cancelledAt
    ) {
        this(
                orderId,
                orderId == null ? null : orderId.toString(),
                userId,
                items,
                totalAmount,
                totalAmount,
                discountAmount,
                shippingFee,
                BigDecimal.ZERO,
                discountAmount,
                finalAmount,
                couponId,
                couponCode,
                couponId,
                couponCode,
                null,
                null,
                paymentMethod,
                paymentStatus,
                status,
                receiverName,
                receiverPhone,
                receiverAddress,
                createdAt,
                updatedAt,
                cancelledAt
        );
    }
}
