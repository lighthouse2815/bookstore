package com.bookstore.bookstore.infrastructure.persistence.projection;

import java.math.BigDecimal;

public interface RevenueStatsProjection {

    String getPeriodKey();

    BigDecimal getRevenue();

    Long getOrderCount();
}
