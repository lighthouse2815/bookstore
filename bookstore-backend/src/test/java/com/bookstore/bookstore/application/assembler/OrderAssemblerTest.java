package com.bookstore.bookstore.application.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderAssemblerTest {

    private final OrderAssembler orderAssembler = new OrderAssembler();

    @Test
    void toResult_mapsOrderAndItems() {
        Order order = order();

        OrderResult result = orderAssembler.toResult(order);

        assertEquals(order.getId(), result.orderId());
        assertEquals(order.getUserId(), result.userId());
        assertEquals(order.getTotalAmount(), result.totalAmount());
        assertEquals(order.getDiscountAmount(), result.discountAmount());
        assertEquals(order.getShippingFee(), result.shippingFee());
        assertEquals(order.getFinalAmount(), result.finalAmount());
        assertEquals(order.getCouponId(), result.couponId());
        assertEquals(order.getCouponCode(), result.couponCode());
        assertEquals(order.getPaymentMethod(), result.paymentMethod());
        assertEquals(order.getPaymentStatus(), result.paymentStatus());
        assertEquals(order.getStatus(), result.status());
        assertEquals(order.getReceiverName(), result.receiverName());
        assertEquals(order.getReceiverPhone(), result.receiverPhone());
        assertEquals(order.getReceiverAddress(), result.receiverAddress());
        assertEquals(1, result.items().size());
        assertEquals(order.getItems().get(0).getId(), result.items().get(0).id());
        assertEquals(order.getItems().get(0).getBookId(), result.items().get(0).bookId());
        assertEquals(order.getItems().get(0).getBookTitle(), result.items().get(0).bookTitle());
        assertEquals(order.getItems().get(0).getUnitPrice(), result.items().get(0).unitPrice());
        assertEquals(order.getItems().get(0).getQuantity(), result.items().get(0).quantity());
        assertEquals(order.getItems().get(0).getLineTotal(), result.items().get(0).lineTotal());
    }

    private static Order order() {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                2,
                new BigDecimal("20.00")
        );

        return new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(item),
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }
}
