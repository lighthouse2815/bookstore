package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.OrderItemResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OrderAssembler {

    public OrderResult toResult(Order order) {
        return toResult(order, null);
    }

    public OrderResult toResult(Order order, Instant paymentExpiresAt) {
        return new OrderResult(
                order.getId(),
                order.getOrderCode(),
                order.getUserId(),
                order.getItems().stream()
                        .map(this::toItemResult)
                        .toList(),
                order.getProductTotal(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getShippingDiscount(),
                order.getCouponDiscount(),
                order.getFinalAmount(),
                order.getCouponId(),
                order.getCouponCode(),
                order.getBookCouponId(),
                order.getBookCouponCode(),
                order.getShippingCouponId(),
                order.getShippingCouponCode(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledAt(),
                paymentExpiresAt
        );
    }

    private OrderItemResult toItemResult(OrderItem item) {
        return new OrderItemResult(
                item.getId(),
                item.getItemType(),
                item.getBookId(),
                item.getDigitalAssetId(),
                item.getBookTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
