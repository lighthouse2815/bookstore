package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void constructor_rejectsEmptyItems() {
        Instant now = Instant.EPOCH;

        DomainException exception = assertThrows(
                DomainException.class,
                () -> new Order(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        null,
                        PaymentMethod.COD,
                        PaymentStatus.PENDING,
                        OrderStatus.PENDING,
                        "Receiver",
                        "0123456789",
                        "Address",
                        now,
                        now,
                        null
                )
        );

        assertEquals(DomainErrorCode.ORDER_MUST_HAVE_AT_LEAST_ONE_ITEM, exception.getErrorCode());
    }

    @Test
    void updateStatus_changesPendingToConfirmed() {
        Order order = order(OrderStatus.PENDING);

        order.updateStatus(OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void cancel_rejectsDeliveredOrder() {
        Order order = order(OrderStatus.DELIVERED);

        DomainException exception = assertThrows(DomainException.class, order::cancel);

        assertEquals(DomainErrorCode.DELIVERED_ORDER_CANNOT_BE_CANCELLED, exception.getErrorCode());
    }

    private static Order order(OrderStatus status) {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                2,
                new BigDecimal("20.00")
        );

        Instant cancelledAt = status == OrderStatus.CANCELLED ? now : null;
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
                status,
                "Receiver",
                "0123456789",
                "Address",
                now,
                now,
                cancelledAt
        );
    }
}
