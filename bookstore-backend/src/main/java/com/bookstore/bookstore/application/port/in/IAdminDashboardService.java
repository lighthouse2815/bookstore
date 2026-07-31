package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.query.RevenueChartQuery;
import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;

import java.util.List;

public interface IAdminDashboardService {

    DashboardSummaryResult getSummary();

    List<RevenueChartResult> getRevenue(RevenueChartQuery query);

    List<TopBookStatsResult> getTopBooks(int limit);

    List<OrderStatusStatsResult> getOrderStatusStats();

    List<LowStockBookResult> getLowStockBooks(int threshold);

    List<RecentOrderResult> getRecentOrders(int limit);
}
