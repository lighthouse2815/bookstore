package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.OrderReportRowResult;
import com.bookstore.bookstore.application.result.RevenueReportRowResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IReviewRepository reviewRepository;

    private AdminReportService adminReportService;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(
                orderRepository,
                bookRepository,
                reviewRepository,
                new CsvExportService()
        );
    }

    @Test
    void exportOrders_includesExpectedColumnsWithoutSensitiveFields() {
        LocalDate from = LocalDate.parse("2026-07-01");
        LocalDate to = LocalDate.parse("2026-07-02");
        when(orderRepository.findOrderReports(any(), any(), eq(OrderStatus.CONFIRMED))).thenReturn(List.of(
                new OrderReportRowResult(
                        UUID.fromString("00000000-0000-0000-0000-000000000101"),
                        "DH-0001",
                        "Nguyễn Văn A",
                        OrderStatus.CONFIRMED,
                        PaymentStatus.PAID,
                        new BigDecimal("125000"),
                        Instant.parse("2026-07-01T03:30:00Z")
                )
        ));

        var report = adminReportService.exportOrders(from, to, OrderStatus.CONFIRMED);
        String csv = new String(report.content(), StandardCharsets.UTF_8);

        assertEquals("orders-report-2026-07-01_to_2026-07-02.csv", report.filename());
        assertTrue(csv.contains("Mã đơn hàng,Order ID,Khách hàng,Trạng thái đơn,Trạng thái thanh toán,Tổng thanh toán,Ngày tạo"));
        assertTrue(csv.contains("DH-0001"));
        assertTrue(csv.contains("Nguyễn Văn A"));
        assertTrue(csv.contains("CONFIRMED"));
        assertTrue(csv.contains("PAID"));
        assertFalse(csv.contains("receiverPhone"));
        assertFalse(csv.contains("receiverAddress"));

        verify(orderRepository).findOrderReports(
                from.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                to.plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                OrderStatus.CONFIRMED
        );
    }

    @Test
    void exportRevenue_fillsMissingDatesWithZeroRows() {
        LocalDate from = LocalDate.parse("2026-07-01");
        LocalDate to = LocalDate.parse("2026-07-03");
        when(orderRepository.findDailyRevenueReports(any(), any())).thenReturn(List.of(
                new RevenueReportRowResult(
                        LocalDate.parse("2026-07-02"),
                        3L,
                        new BigDecimal("450000"),
                        1L
                )
        ));

        var report = adminReportService.exportRevenue(from, to);
        String csv = new String(report.content(), StandardCharsets.UTF_8);

        assertTrue(csv.contains("2026-07-01,0,0,0"));
        assertTrue(csv.contains("2026-07-02,3,450000,1"));
        assertTrue(csv.contains("2026-07-03,0,0,0"));
    }
}
