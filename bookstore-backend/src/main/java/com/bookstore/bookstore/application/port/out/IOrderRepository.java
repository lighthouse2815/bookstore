package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.OrderReportRowResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueReportRowResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.model.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IOrderRepository {

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByIdForUpdate(UUID orderId);

    Optional<Order> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<Order> findByUserIdAndIdempotencyKeyForUpdate(UUID userId, String idempotencyKey);

    List<Order> findByUserId(UUID userId);

    PageSliceResult<Order> findPageByUserId(UUID userId, int page, int size);

    Map<UUID, Long> countDeliveredQuantityByBookIds(Collection<UUID> bookIds);

    BigDecimal sumDeliveredRevenueBetween(Instant fromInclusive, Instant toExclusive);

    long countCreatedBetween(Instant fromInclusive, Instant toExclusive);

    long countByStatus(OrderStatus status);

    List<RevenueChartResult> findRevenueStatsGroupByDay(Instant fromInclusive, Instant toExclusive);

    List<RevenueChartResult> findRevenueStatsGroupByMonth(Instant fromInclusive, Instant toExclusive);

    List<OrderReportRowResult> findOrderReports(
            Instant fromInclusive,
            Instant toExclusive,
            OrderStatus status
    );

    List<RevenueReportRowResult> findDailyRevenueReports(Instant fromInclusive, Instant toExclusive);

    List<TopBookStatsResult> findTopSellingBooks(int limit);

    List<OrderStatusStatsResult> countOrdersByStatus();

    List<RecentOrderResult> findRecentOrders(int limit);

    List<Order> findAll();

    PageSliceResult<Order> findPageAll(int page, int size);

    Order save(Order order);
}
