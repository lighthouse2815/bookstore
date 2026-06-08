package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.OrderItemResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderAssembler {

    public OrderResult toResult(Order order) {
        return new OrderResult(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(this::toItemResult)
                        .toList(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getFinalAmount(),
                order.getCouponId(),
                order.getCouponCode(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledAt()
        );
    }

    private OrderItemResult toItemResult(OrderItem item) {
        return new OrderItemResult(
                item.getId(),
                item.getBookId(),
                item.getBookTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
