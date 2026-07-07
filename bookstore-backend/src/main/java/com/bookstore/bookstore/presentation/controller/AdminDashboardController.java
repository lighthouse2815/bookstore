package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAdminDashboardService;
import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.dashboard.DashboardSummaryResponse;
import com.bookstore.bookstore.presentation.response.dashboard.LowStockBookResponse;
import com.bookstore.bookstore.presentation.response.dashboard.OrderStatusStatsResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RecentOrderResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RevenueChartResponse;
import com.bookstore.bookstore.presentation.response.dashboard.TopBookStatsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final IAdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(toSummaryResponse(adminDashboardService.getSummary()));
    }

    @GetMapping("/revenue")
    public ApiResponse<List<RevenueChartResponse>> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") String groupBy
    ) {
        return ApiResponse.success(adminDashboardService.getRevenue(from, to, groupBy).stream()
                .map(this::toRevenueResponse)
                .toList());
    }

    @GetMapping("/top-books")
    public ApiResponse<List<TopBookStatsResponse>> getTopBooks(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ApiResponse.success(adminDashboardService.getTopBooks(limit).stream()
                .map(this::toTopBookResponse)
                .toList());
    }

    @GetMapping("/orders/status")
    public ApiResponse<List<OrderStatusStatsResponse>> getOrderStatusStats() {
        return ApiResponse.success(adminDashboardService.getOrderStatusStats().stream()
                .map(this::toOrderStatusResponse)
                .toList());
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<LowStockBookResponse>> getLowStockBooks(
            @RequestParam(defaultValue = "10") @Min(0) int threshold
    ) {
        return ApiResponse.success(adminDashboardService.getLowStockBooks(threshold).stream()
                .map(this::toLowStockResponse)
                .toList());
    }

    @GetMapping("/recent-orders")
    public ApiResponse<List<RecentOrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ApiResponse.success(adminDashboardService.getRecentOrders(limit).stream()
                .map(this::toRecentOrderResponse)
                .toList());
    }

    private DashboardSummaryResponse toSummaryResponse(DashboardSummaryResult result) {
        return new DashboardSummaryResponse(
                result.todayRevenue(),
                result.monthRevenue(),
                result.todayOrders(),
                result.pendingOrders(),
                result.lowStockBooks(),
                result.newCustomers(),
                result.newReviews(),
                result.activeCoupons()
        );
    }

    private RevenueChartResponse toRevenueResponse(RevenueChartResult result) {
        return new RevenueChartResponse(
                result.label(),
                result.revenue(),
                result.orders()
        );
    }

    private TopBookStatsResponse toTopBookResponse(TopBookStatsResult result) {
        return new TopBookStatsResponse(
                result.bookId(),
                result.title(),
                result.soldQuantity(),
                result.revenue()
        );
    }

    private OrderStatusStatsResponse toOrderStatusResponse(OrderStatusStatsResult result) {
        return new OrderStatusStatsResponse(
                result.status() == null ? null : result.status().name(),
                result.count()
        );
    }

    private LowStockBookResponse toLowStockResponse(LowStockBookResult result) {
        return new LowStockBookResponse(
                result.bookId(),
                result.title(),
                result.stockQuantity()
        );
    }

    private RecentOrderResponse toRecentOrderResponse(RecentOrderResult result) {
        return new RecentOrderResponse(
                result.orderId(),
                result.orderCode(),
                result.customerName(),
                result.finalAmount(),
                result.status() == null ? null : result.status().name(),
                result.createdAt()
        );
    }
}
