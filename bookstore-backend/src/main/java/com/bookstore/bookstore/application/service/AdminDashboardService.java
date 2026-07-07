package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.port.in.IAdminDashboardService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.DashboardSummaryResult;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService implements IAdminDashboardService {
    
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_THRESHOLD = 10;
    private static final int DEFAULT_REVENUE_DAYS = 30;
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;
    private final IReviewRepository reviewRepository;
    private final ICouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResult getSummary() {
        LocalDate today = LocalDate.now();
        Instant now = Instant.now();
        Instant todayStart = toStartOfDay(today);
        Instant tomorrowStart = toStartOfDay(today.plusDays(1));
        Instant monthStart = toStartOfDay(today.withDayOfMonth(1));

        return new DashboardSummaryResult(
                orderRepository.sumDeliveredRevenueBetween(todayStart, tomorrowStart),
                orderRepository.sumDeliveredRevenueBetween(monthStart, tomorrowStart),
                orderRepository.countCreatedBetween(todayStart, tomorrowStart),
                orderRepository.countByStatus(OrderStatus.PENDING),
                bookRepository.countLowStockBooks(DEFAULT_THRESHOLD),
                userRepository.countNewCustomersBetween(todayStart, tomorrowStart),
                reviewRepository.countNewReviewsBetween(todayStart, tomorrowStart),
                couponRepository.countActiveCouponsAt(now)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueChartResult> getRevenue(LocalDate from, LocalDate to, String groupBy) {
        DateRange range = resolveDateRange(from, to);
        RevenueGroupBy resolvedGroupBy = resolveGroupBy(groupBy);
        Instant fromInclusive = toStartOfDay(range.from());
        Instant toExclusive = toStartOfDay(range.to().plusDays(1));

        List<RevenueChartResult> rawStats = resolvedGroupBy == RevenueGroupBy.MONTH
                ? orderRepository.findRevenueStatsGroupByMonth(fromInclusive, toExclusive)
                : orderRepository.findRevenueStatsGroupByDay(fromInclusive, toExclusive);

        Map<String, RevenueChartResult> statsByKey = rawStats.stream()
                .collect(Collectors.toMap(RevenueChartResult::label, Function.identity()));

        return resolvedGroupBy == RevenueGroupBy.MONTH
                ? buildMonthlyRevenueChart(range, statsByKey)
                : buildDailyRevenueChart(range, statsByKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopBookStatsResult> getTopBooks(int limit) {
        return orderRepository.findTopSellingBooks(validateLimit(limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusStatsResult> getOrderStatusStats() {
        return orderRepository.countOrdersByStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockBookResult> getLowStockBooks(int threshold) {
        return bookRepository.findLowStockBooks(validateThreshold(threshold));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentOrderResult> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(validateLimit(limit));
    }

    private List<RevenueChartResult> buildDailyRevenueChart(
            DateRange range,
            Map<String, RevenueChartResult> statsByKey
    ) {
        List<RevenueChartResult> results = new ArrayList<>();
        for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1)) {
            String key = date.toString();
            RevenueChartResult stat = statsByKey.get(key);
            results.add(new RevenueChartResult(
                    date.format(DAY_LABEL_FORMATTER),
                    stat == null ? BigDecimal.ZERO : stat.revenue(),
                    stat == null ? 0L : stat.orders()
            ));
        }
        return results;
    }

    private List<RevenueChartResult> buildMonthlyRevenueChart(
            DateRange range,
            Map<String, RevenueChartResult> statsByKey
    ) {
        List<RevenueChartResult> results = new ArrayList<>();
        LocalDate cursor = range.from().withDayOfMonth(1);
        LocalDate endMonth = range.to().withDayOfMonth(1);

        while (!cursor.isAfter(endMonth)) {
            String key = cursor.format(MONTH_KEY_FORMATTER);
            RevenueChartResult stat = statsByKey.get(key);
            results.add(new RevenueChartResult(
                    cursor.format(MONTH_LABEL_FORMATTER),
                    stat == null ? BigDecimal.ZERO : stat.revenue(),
                    stat == null ? 0L : stat.orders()
            ));
            cursor = cursor.plusMonths(1);
        }

        return results;
    }

    private DateRange resolveDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            LocalDate today = LocalDate.now();
            return new DateRange(today.minusDays(DEFAULT_REVENUE_DAYS - 1L), today);
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        return new DateRange(from, to);
    }

    private RevenueGroupBy resolveGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return RevenueGroupBy.DAY;
        }

        return RevenueGroupBy.valueOf(groupBy.trim().toUpperCase());
    }

    private int validateLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and 50");
        }

        return limit;
    }

    private int validateThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be greater than or equal to 0");
        }

        return threshold;
    }

    private Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private enum RevenueGroupBy {
        DAY,
        MONTH
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
