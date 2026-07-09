package com.bookstore.bookstore.infrastructure.persistence.projection;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface OrderReportProjection {

    UUID getOrderId();

    String getOrderCode();

    String getCustomerName();

    OrderStatus getStatus();

    PaymentStatus getPaymentStatus();

    BigDecimal getFinalAmount();

    Instant getCreatedAt();
}
