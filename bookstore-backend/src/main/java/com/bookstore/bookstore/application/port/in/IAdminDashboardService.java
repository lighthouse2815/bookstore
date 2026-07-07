package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;

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
