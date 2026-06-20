package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.dashboard.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.dashboard.LowStockBookResult;
import com.bookstore.bookstore.application.result.dashboard.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.dashboard.RecentOrderResult;
import com.bookstore.bookstore.application.result.dashboard.RevenueChartResult;
import com.bookstore.bookstore.application.result.dashboard.TopBookStatsResult;
import java.time.LocalDate;
import java.util.List;

public interface IAdminDashboardService {

    DashboardSummaryResult getSummary();

    List<RevenueChartResult> getRevenue(LocalDate from, LocalDate to, String groupBy);

    List<TopBookStatsResult> getTopBooks(int limit);

    List<OrderStatusStatsResult> getOrderStatusStats();

    List<LowStockBookResult> getLowStockBooks(int threshold);

    List<RecentOrderResult> getRecentOrders(int limit);
}
