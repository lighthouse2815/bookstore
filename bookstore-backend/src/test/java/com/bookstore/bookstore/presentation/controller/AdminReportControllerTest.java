package com.bookstore.bookstore.presentation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IAdminReportService;
import com.bookstore.bookstore.application.result.ReportFileResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminReportController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAdminReportService adminReportService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void exportOrders_whenStaffAuthenticated_returnsCsvWithHeadersAndFilters() throws Exception {
        given(adminReportService.exportOrders(
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-07-08"),
                OrderStatus.CONFIRMED
        )).willReturn(report("orders-report.csv", "Mã đơn hàng\r\nDH-0001\r\n"));

        mockMvc.perform(get("/api/admin/reports/orders.csv")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-08")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"orders-report.csv\""))
                .andExpect(content().bytes(report("orders-report.csv", "Mã đơn hàng\r\nDH-0001\r\n").content()));

        verify(adminReportService).exportOrders(
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-07-08"),
                OrderStatus.CONFIRMED
        );
    }

    @Test
    void exportRevenue_whenAdminAuthenticated_returnsCsvWithHeaders() throws Exception {
        given(adminReportService.exportRevenue(
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-07-08")
        )).willReturn(report("revenue-report.csv", "Ngày\r\n2026-07-01\r\n"));

        mockMvc.perform(get("/api/admin/reports/revenue.csv")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-08"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"revenue-report.csv\""));

        verify(adminReportService).exportRevenue(
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-07-08")
        );
    }

    @Test
    void exportLowStock_whenAdminAuthenticated_returnsCsvWithHeaders() throws Exception {
        given(adminReportService.exportLowStock(5))
                .willReturn(report("low-stock-report.csv", "Book ID\r\nbook-1\r\n"));

        mockMvc.perform(get("/api/admin/reports/low-stock.csv")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"low-stock-report.csv\""));

        verify(adminReportService).exportLowStock(5);
    }

    @Test
    void exportReviews_whenAdminAuthenticated_returnsCsvWithHeadersAndStatusFilter() throws Exception {
        given(adminReportService.exportReviews(ReviewStatus.HIDDEN))
                .willReturn(report("reviews-report.csv", "Tên sách\r\nSách A\r\n"));

        mockMvc.perform(get("/api/admin/reports/reviews.csv")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("status", "HIDDEN"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"reviews-report.csv\""));

        verify(adminReportService).exportReviews(ReviewStatus.HIDDEN);
    }

    private ReportFileResult report(String filename, String body) {
        return new ReportFileResult(filename, ("\uFEFF" + body).getBytes(StandardCharsets.UTF_8));
    }
}
