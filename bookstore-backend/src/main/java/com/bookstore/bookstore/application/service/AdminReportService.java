package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAdminReportService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.query.ExportOrdersQuery;
import com.bookstore.bookstore.application.result.LowStockReportRowResult;
import com.bookstore.bookstore.application.result.OrderReportRowResult;
import com.bookstore.bookstore.application.result.ReportFileResult;
import com.bookstore.bookstore.application.result.RevenueReportRowResult;
import com.bookstore.bookstore.application.result.ReviewReportRowResult;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.shared.time.BusinessTime;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService implements IAdminReportService {

    private static final int DEFAULT_EXPORT_DAYS = 30;
    private static final int MAX_EXPORT_DAYS = 366;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;
    private final IReviewRepository reviewRepository;
    private final CsvExportService csvExportService;

    private final BusinessTime businessTime;

    @Override
    @Transactional(readOnly = true)
    public ReportFileResult exportOrders(ExportOrdersQuery query) {
        DateRange range = resolveDateRange(query.from(), query.to());
        List<OrderReportRowResult> rows = orderRepository.findOrderReports(
                businessTime.startOfDayInstant(range.from()),
                businessTime.startOfDayInstant(range.to().plusDays(1L)),
                query.status()
        );
        return new ReportFileResult(
                "orders-report-" + range.toFilenameSegment() + ".csv",
                csvExportService.export(
                        List.of(
                                "Mã đơn hàng",
                                "Order ID",
                                "Khách hàng",
                                "Trạng thái đơn",
                                "Trạng thái thanh toán",
                                "Tổng thanh toán",
                                "Ngày tạo"
                        ),
                        rows.stream().map(this::toOrderCsvRow).toList()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFileResult exportRevenue(LocalDate from, LocalDate to) {
        DateRange range = resolveDateRange(from, to);
        Map<LocalDate, RevenueReportRowResult> statsByDate = new LinkedHashMap<>();
        for (RevenueReportRowResult row : orderRepository.findDailyRevenueReports(
                businessTime.startOfDayInstant(range.from()),
                businessTime.startOfDayInstant(range.to().plusDays(1L))
        )) {
            statsByDate.put(row.date(), row);
        }

        List<RevenueReportRowResult> rows = buildRevenueRows(range, statsByDate);
        return new ReportFileResult(
                "revenue-report-" + range.toFilenameSegment() + ".csv",
                csvExportService.export(
                        List.of(
                                "Ngày",
                                "Tổng đơn hàng",
                                "Doanh thu",
                                "Đơn hủy"
                        ),
                        rows.stream().map(this::toRevenueCsvRow).toList()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFileResult exportLowStock(int threshold) {
        int resolvedThreshold = validateThreshold(threshold);
        List<LowStockReportRowResult> rows = bookRepository.findLowStockReportRows(resolvedThreshold);
        return new ReportFileResult(
                "low-stock-report-threshold-" + resolvedThreshold + ".csv",
                csvExportService.export(
                        List.of(
                                "Book ID",
                                "Tên sách",
                                "SKU/ISBN",
                                "Tồn kho",
                                "Danh mục"
                        ),
                        rows.stream().map(this::toLowStockCsvRow).toList()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFileResult exportReviews(ReviewStatus status) {
        List<ReviewReportRowResult> rows = reviewRepository.findReviewReportRows(status);
        String statusSegment = status == null ? "all" : status.name().toLowerCase(Locale.ROOT);
        return new ReportFileResult(
                "reviews-report-" + statusSegment + ".csv",
                csvExportService.export(
                        List.of(
                                "Tên sách",
                                "Người dùng",
                                "Số sao",
                                "Trạng thái",
                                "Ngày tạo",
                                "Lý do moderation"
                        ),
                        rows.stream().map(this::toReviewCsvRow).toList()
                )
        );
    }

    private List<RevenueReportRowResult> buildRevenueRows(
            DateRange range,
            Map<LocalDate, RevenueReportRowResult> statsByDate
    ) {
        List<RevenueReportRowResult> rows = new java.util.ArrayList<>();
        for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1L)) {
            RevenueReportRowResult stat = statsByDate.get(date);
            rows.add(stat == null
                    ? new RevenueReportRowResult(date, 0L, BigDecimal.ZERO, 0L)
                    : stat);
        }
        return rows;
    }

    private List<?> toOrderCsvRow(OrderReportRowResult row) {
        return List.of(
                safeText(row.orderCode()),
                safeText(row.orderId()),
                safeText(row.customerName()),
                safeText(row.status()),
                safeText(row.paymentStatus()),
                formatAmount(row.finalAmount()),
                formatInstant(row.createdAt())
        );
    }

    private List<?> toRevenueCsvRow(RevenueReportRowResult row) {
        return List.of(
                row.date() == null ? "" : row.date().format(DATE_FORMATTER),
                row.totalOrders(),
                formatAmount(row.revenue()),
                row.cancelledOrders()
        );
    }

    private List<?> toLowStockCsvRow(LowStockReportRowResult row) {
        return List.of(
                safeText(row.bookId()),
                safeText(row.title()),
                safeText(row.isbn()),
                row.stockQuantity(),
                safeText(row.categoryName())
        );
    }

    private List<?> toReviewCsvRow(ReviewReportRowResult row) {
        return List.of(
                safeText(row.bookTitle()),
                safeText(row.username()),
                row.rating(),
                safeText(row.status()),
                formatInstant(row.createdAt()),
                safeText(row.moderationReason())
        );
    }

    private DateRange resolveDateRange(LocalDate from, LocalDate to) {
        LocalDate today = businessTime.todayLocalDate();
        LocalDate resolvedFrom = from;
        LocalDate resolvedTo = to;

        if (resolvedFrom == null && resolvedTo == null) {
            resolvedTo = today;
            resolvedFrom = today.minusDays(DEFAULT_EXPORT_DAYS - 1L);
        } else if (resolvedFrom == null) {
            resolvedFrom = resolvedTo.minusDays(DEFAULT_EXPORT_DAYS - 1L);
        } else if (resolvedTo == null) {
            resolvedTo = today;
        }

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "from");
        }

        long totalDays = ChronoUnit.DAYS.between(resolvedFrom, resolvedTo) + 1L;
        if (totalDays > MAX_EXPORT_DAYS) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "dateRange");
        }

        return new DateRange(resolvedFrom, resolvedTo);
    }

    private int validateThreshold(int threshold) {
        if (threshold < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "threshold");
        }
        return threshold;
    }

    private String formatInstant(Instant value) {
        if (value == null) {
            return "";
        }
         return DATE_TIME_FORMATTER.format(
            businessTime.toZonedDateTime(value)
        );
    }

    private String formatAmount(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String safeText(Object value) {
        return value == null ? "" : value.toString();
    }

    private record DateRange(LocalDate from, LocalDate to) {
        private String toFilenameSegment() {
            return from.format(DATE_FORMATTER) + "_to_" + to.format(DATE_FORMATTER);
        }
    }
}
