package com.bookstore.bookstore.infrastructure.persistence.projection;

import java.math.BigDecimal;

public interface DailyRevenueReportProjection {

    String getPeriodKey();

    Long getTotalOrders();

    BigDecimal getRevenue();

    Long getCancelledOrders();
}
