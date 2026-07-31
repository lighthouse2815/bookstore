package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAdminDashboardService;
import com.bookstore.bookstore.presentation.mapper.AdminDashboardWebMapper;
import com.bookstore.bookstore.presentation.request.RevenueChartRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.dashboard.DashboardSummaryResponse;
import com.bookstore.bookstore.presentation.response.dashboard.LowStockBookResponse;
import com.bookstore.bookstore.presentation.response.dashboard.OrderStatusStatsResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RecentOrderResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RevenueChartResponse;
import com.bookstore.bookstore.presentation.response.dashboard.TopBookStatsResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final AdminDashboardWebMapper adminDashboardWebMapper;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(adminDashboardWebMapper.toSummaryResponse(adminDashboardService.getSummary()));
    }

    @GetMapping("/revenue")
    public ApiResponse<List<RevenueChartResponse>> getRevenue(
            @Valid @ModelAttribute RevenueChartRequest request
    ) {
        var query = adminDashboardWebMapper.toRevenueQuery(request);
        return ApiResponse.success(adminDashboardService.getRevenue(query).stream()
                .map(adminDashboardWebMapper::toRevenueResponse)
                .toList());
    }

    @GetMapping("/top-books")
    public ApiResponse<List<TopBookStatsResponse>> getTopBooks(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ApiResponse.success(adminDashboardService.getTopBooks(limit).stream()
                .map(adminDashboardWebMapper::toTopBookResponse)
                .toList());
    }

    @GetMapping("/orders/status")
    public ApiResponse<List<OrderStatusStatsResponse>> getOrderStatusStats() {
        return ApiResponse.success(adminDashboardService.getOrderStatusStats().stream()
                .map(adminDashboardWebMapper::toOrderStatusResponse)
                .toList());
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<LowStockBookResponse>> getLowStockBooks(
            @RequestParam(defaultValue = "10") @Min(0) int threshold
    ) {
        return ApiResponse.success(adminDashboardService.getLowStockBooks(threshold).stream()
                .map(adminDashboardWebMapper::toLowStockResponse)
                .toList());
    }

    @GetMapping("/recent-orders")
    public ApiResponse<List<RecentOrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ApiResponse.success(adminDashboardService.getRecentOrders(limit).stream()
                .map(adminDashboardWebMapper::toRecentOrderResponse)
                .toList());
    }
}
