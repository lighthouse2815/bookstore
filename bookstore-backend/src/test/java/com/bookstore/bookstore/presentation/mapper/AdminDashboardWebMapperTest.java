package com.bookstore.bookstore.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.bookstore.bookstore.application.enums.RevenueGroupBy;
import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.presentation.request.RevenueChartRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminDashboardWebMapperTest {

    private final AdminDashboardWebMapper mapper = new AdminDashboardWebMapper();

    @Test
    void toRevenueQuery_mapsHttpFilterToTypedApplicationQuery() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);

        var query = mapper.toRevenueQuery(new RevenueChartRequest(from, to, "month"));

        assertEquals(from, query.from());
        assertEquals(to, query.to());
        assertEquals(RevenueGroupBy.MONTH, query.groupBy());
    }

    @Test
    void toSummaryResponse_mapsAllMetrics() {
        DashboardSummaryResult result = new DashboardSummaryResult(
                new BigDecimal("1000"),
                new BigDecimal("100"),
                new BigDecimal("500"),
                20,
                2,
                3,
                10,
                1,
                50,
                100,
                4,
                5,
                6,
                7
        );

        var response = mapper.toSummaryResponse(result);

        assertEquals(result.totalRevenue(), response.totalRevenue());
        assertEquals(result.todayRevenue(), response.todayRevenue());
        assertEquals(result.monthRevenue(), response.monthRevenue());
        assertEquals(result.totalOrders(), response.totalOrders());
        assertEquals(result.activeCoupons(), response.activeCoupons());
    }

    @Test
    void toRevenueResponse_mapsChartPoint() {
        RevenueChartResult result = new RevenueChartResult("27/07", new BigDecimal("250"), 4);

        var response = mapper.toRevenueResponse(result);

        assertEquals(result.label(), response.label());
        assertEquals(result.revenue(), response.revenue());
        assertEquals(result.orders(), response.orders());
    }

    @Test
    void toTopBookResponse_mapsBookStatistics() {
        TopBookStatsResult result = new TopBookStatsResult(
                UUID.randomUUID(),
                "Domain-Driven Design",
                12,
                new BigDecimal("1200")
        );

        var response = mapper.toTopBookResponse(result);

        assertEquals(result.bookId(), response.bookId());
        assertEquals(result.title(), response.title());
        assertEquals(result.soldQuantity(), response.soldQuantity());
        assertEquals(result.revenue(), response.revenue());
    }

    @Test
    void toOrderStatusResponse_mapsEnumToApiString() {
        var response = mapper.toOrderStatusResponse(new OrderStatusStatsResult(OrderStatus.DELIVERED, 8));

        assertEquals("DELIVERED", response.status());
        assertEquals(8, response.count());
    }

    @Test
    void toLowStockResponse_mapsBookStock() {
        LowStockBookResult result = new LowStockBookResult(UUID.randomUUID(), "Clean Architecture", 2);

        var response = mapper.toLowStockResponse(result);

        assertEquals(result.bookId(), response.bookId());
        assertEquals(result.title(), response.title());
        assertEquals(result.stockQuantity(), response.stockQuantity());
    }

    @Test
    void toRecentOrderResponse_preservesNullStatusContract() {
        RecentOrderResult result = new RecentOrderResult(
                UUID.randomUUID(),
                "ORD-001",
                "Nguyễn Văn A",
                new BigDecimal("350"),
                null,
                Instant.EPOCH
        );

        var response = mapper.toRecentOrderResponse(result);

        assertEquals(result.orderId(), response.orderId());
        assertEquals(result.orderCode(), response.orderCode());
        assertEquals(result.customerName(), response.customerName());
        assertEquals(result.finalAmount(), response.finalAmount());
        assertNull(response.status());
        assertEquals(result.createdAt(), response.createdAt());
    }
}
