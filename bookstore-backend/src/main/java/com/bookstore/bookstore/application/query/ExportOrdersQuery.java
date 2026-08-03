package com.bookstore.bookstore.application.query;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.time.LocalDate;

public record ExportOrdersQuery(
        LocalDate from,
        LocalDate to,
        OrderStatus status
) {
    public ExportOrdersQuery {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("from và to phải được cung cấp cùng nhau");
        }

        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from không được sau to");
        }
    }
}
