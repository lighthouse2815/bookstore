package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.dashboard.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.dashboard.RecentOrderResult;
import com.bookstore.bookstore.application.result.dashboard.RevenueChartResult;
import com.bookstore.bookstore.application.result.dashboard.TopBookStatsResult;
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

    List<Order> findByUserId(UUID userId);

    Map<UUID, Long> countDeliveredQuantityByBookIds(Collection<UUID> bookIds);

    BigDecimal sumDeliveredRevenueBetween(Instant fromInclusive, Instant toExclusive);

    long countCreatedBetween(Instant fromInclusive, Instant toExclusive);

    long countByStatus(OrderStatus status);

    List<RevenueChartResult> findRevenueStatsGroupByDay(Instant fromInclusive, Instant toExclusive);

    List<RevenueChartResult> findRevenueStatsGroupByMonth(Instant fromInclusive, Instant toExclusive);

    List<TopBookStatsResult> findTopSellingBooks(int limit);

    List<OrderStatusStatsResult> countOrdersByStatus();

    List<RecentOrderResult> findRecentOrders(int limit);

    List<Order> findAll();

    Order save(Order order);
}
