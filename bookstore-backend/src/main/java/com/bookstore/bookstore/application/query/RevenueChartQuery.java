package com.bookstore.bookstore.application.query;

import com.bookstore.bookstore.application.enums.RevenueGroupBy;
import java.time.LocalDate;

public record RevenueChartQuery(
        LocalDate from,
        LocalDate to,
        RevenueGroupBy groupBy
) {

    public RevenueChartQuery {
        groupBy = groupBy == null ? RevenueGroupBy.DAY : groupBy;

        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("from và to phải được cung cấp cùng nhau");
        }

        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from không được sau to");
        }
    }
}
