package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.ReportFileResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import java.time.LocalDate;

public interface IAdminReportService {

    ReportFileResult exportOrders(LocalDate from, LocalDate to, OrderStatus status);

    ReportFileResult exportRevenue(LocalDate from, LocalDate to);

    ReportFileResult exportLowStock(int threshold);

    ReportFileResult exportReviews(ReviewStatus status);
}
