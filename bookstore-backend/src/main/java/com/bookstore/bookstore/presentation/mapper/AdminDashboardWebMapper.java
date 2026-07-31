package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.enums.RevenueGroupBy;
import com.bookstore.bookstore.application.query.RevenueChartQuery;
import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.presentation.request.RevenueChartRequest;
import com.bookstore.bookstore.presentation.response.dashboard.DashboardSummaryResponse;
import com.bookstore.bookstore.presentation.response.dashboard.LowStockBookResponse;
import com.bookstore.bookstore.presentation.response.dashboard.OrderStatusStatsResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RecentOrderResponse;
import com.bookstore.bookstore.presentation.response.dashboard.RevenueChartResponse;
import com.bookstore.bookstore.presentation.response.dashboard.TopBookStatsResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminDashboardWebMapper {

    public RevenueChartQuery toRevenueQuery(RevenueChartRequest request) {
        return new RevenueChartQuery(
                request.from(),
                request.to(),
                RevenueGroupBy.valueOf(request.groupBy())
        );
    }

    public DashboardSummaryResponse toSummaryResponse(DashboardSummaryResult result) {
        return new DashboardSummaryResponse(
                result.totalRevenue(),
                result.todayRevenue(),
                result.monthRevenue(),
                result.totalOrders(),
                result.todayOrders(),
                result.pendingOrders(),
                result.deliveredOrders(),
                result.cancelledOrders(),
                result.totalUsers(),
                result.totalBooks(),
                result.lowStockBooks(),
                result.newCustomers(),
                result.newReviews(),
                result.activeCoupons()
        );
    }

    public RevenueChartResponse toRevenueResponse(RevenueChartResult result) {
        return new RevenueChartResponse(
                result.label(),
                result.revenue(),
                result.orders()
        );
    }

    public TopBookStatsResponse toTopBookResponse(TopBookStatsResult result) {
        return new TopBookStatsResponse(
                result.bookId(),
                result.title(),
                result.soldQuantity(),
                result.revenue()
        );
    }

    public OrderStatusStatsResponse toOrderStatusResponse(OrderStatusStatsResult result) {
        return new OrderStatusStatsResponse(
                result.status() == null ? null : result.status().name(),
                result.count()
        );
    }

    public LowStockBookResponse toLowStockResponse(LowStockBookResult result) {
        return new LowStockBookResponse(
                result.bookId(),
                result.title(),
                result.stockQuantity()
        );
    }

    public RecentOrderResponse toRecentOrderResponse(RecentOrderResult result) {
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
