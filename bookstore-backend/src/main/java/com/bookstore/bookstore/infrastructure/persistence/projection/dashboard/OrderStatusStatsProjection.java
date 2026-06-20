package com.bookstore.bookstore.infrastructure.persistence.projection.dashboard;

import com.bookstore.bookstore.domain.enums.OrderStatus;

public interface OrderStatusStatsProjection {

    OrderStatus getStatus();

    Long getCount();
}
