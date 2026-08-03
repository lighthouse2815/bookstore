package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.query.ExportOrdersQuery;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AdminReportMapper {

    public ExportOrdersQuery toExportOrdersQuery(
            LocalDate from,
            LocalDate to,
            OrderStatus status
    ) {
        return new ExportOrdersQuery(from, to, status);
    }
}
