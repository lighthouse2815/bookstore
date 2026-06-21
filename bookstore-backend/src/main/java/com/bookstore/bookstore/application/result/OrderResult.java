package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID orderId,
        String orderCode,
        UUID userId,
        List<OrderItemResult> items,
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
    public OrderResult(
            UUID orderId,
            UUID userId,
            List<OrderItemResult> items,
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

    public OrderResult {
        items = items == null ? List.of() : List.copyOf(items);
        productTotal = productTotal == null ? BigDecimal.ZERO : productTotal;
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        shippingFee = shippingFee == null ? BigDecimal.ZERO : shippingFee;
        shippingDiscount = shippingDiscount == null ? BigDecimal.ZERO : shippingDiscount;
        couponDiscount = couponDiscount == null ? BigDecimal.ZERO : couponDiscount;
        finalAmount = finalAmount == null ? BigDecimal.ZERO : finalAmount;
    }
}
