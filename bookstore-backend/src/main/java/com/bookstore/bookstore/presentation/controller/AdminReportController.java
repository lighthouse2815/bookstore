package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAdminReportService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.result.ReportFileResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final IAdminReportService adminReportService;
    private final AdminAuditSupport adminAuditSupport;

    @GetMapping("/orders.csv")
    public ResponseEntity<byte[]> exportOrders(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OrderStatus status
    ) {
        ReportFileResult report = adminReportService.exportOrders(from, to, status);
        recordExport(
                jwt,
                httpServletRequest,
                "orders",
                "Xuất báo cáo đơn hàng CSV",
                report,
                buildMetadata(from, to, status == null ? null : status.name(), null)
        );
        return buildCsvResponse(report);
    }

    @GetMapping("/revenue.csv")
    public ResponseEntity<byte[]> exportRevenue(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ReportFileResult report = adminReportService.exportRevenue(from, to);
        recordExport(
                jwt,
                httpServletRequest,
                "revenue",
                "Xuất báo cáo doanh thu CSV",
                report,
                buildMetadata(from, to, null, null)
        );
        return buildCsvResponse(report);
    }

    @GetMapping("/low-stock.csv")
    public ResponseEntity<byte[]> exportLowStock(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @RequestParam(defaultValue = "10") @Min(0) int threshold
    ) {
        ReportFileResult report = adminReportService.exportLowStock(threshold);
        recordExport(
                jwt,
                httpServletRequest,
                "low-stock",
                "Xuất báo cáo tồn kho thấp CSV",
                report,
                buildMetadata(null, null, null, threshold)
        );
        return buildCsvResponse(report);
    }

    @GetMapping("/reviews.csv")
    public ResponseEntity<byte[]> exportReviews(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @RequestParam(required = false) ReviewStatus status
    ) {
        ReportFileResult report = adminReportService.exportReviews(status);
        recordExport(
                jwt,
                httpServletRequest,
                "reviews",
                "Xuất báo cáo review moderation CSV",
                report,
                buildMetadata(null, null, status == null ? null : status.name(), null)
        );
        return buildCsvResponse(report);
    }

    private ResponseEntity<byte[]> buildCsvResponse(ReportFileResult report) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(report.content());
    }

    private void recordExport(
            Jwt jwt,
            HttpServletRequest httpServletRequest,
            String reportKey,
            String description,
            ReportFileResult report,
            Map<String, Object> metadata
    ) {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("filename", report.filename());
        if (!metadata.isEmpty()) {
            payload.put("filters", metadata);
        }
        adminAuditSupport.recordCreate(
                jwt,
                httpServletRequest,
                "REPORT_EXPORTED",
                AuditTargetType.REPORT,
                reportKey,
                description,
                payload
        );
    }

    private Map<String, Object> buildMetadata(
            LocalDate from,
            LocalDate to,
            String status,
            Integer threshold
    ) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        if (from != null) {
            metadata.put("from", from.toString());
        }
        if (to != null) {
            metadata.put("to", to.toString());
        }
        if (status != null && !status.isBlank()) {
            metadata.put("status", status);
        }
        if (threshold != null) {
            metadata.put("threshold", threshold);
        }
        return metadata;
    }
}
