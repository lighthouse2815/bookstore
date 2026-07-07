package com.bookstore.bookstore.infrastructure.persistence.projection;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecentOrderProjection {

    UUID getOrderId();

    String getOrderCode();

    String getCustomerName();

    BigDecimal getFinalAmount();

    OrderStatus getStatus();

    Instant getCreatedAt();
}
