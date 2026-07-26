package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.result.OrderReportRowResult;
import com.bookstore.bookstore.application.result.OrderStatusStatsResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.RecentOrderResult;
import com.bookstore.bookstore.application.result.RevenueReportRowResult;
import com.bookstore.bookstore.application.result.RevenueChartResult;
import com.bookstore.bookstore.application.result.TopBookStatsResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.DigitalAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements IOrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final DigitalAssetJpaRepository digitalAssetJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findByIdAndUser_DeletedAtIsNull(orderId)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdForUpdate(UUID orderId) {
        Optional<OrderJpaEntity> lockedOrder = orderJpaRepository.findByIdAndUser_DeletedAtIsNullForUpdate(orderId);
        if (lockedOrder.isEmpty()) {
            return Optional.empty();
        }

        return orderJpaRepository.findByIdAndUser_DeletedAtIsNull(orderId)
                .or(() -> lockedOrder)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Order> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return orderJpaRepository.findByUser_IdAndIdempotencyKey(userId, idempotencyKey)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Order> findByUserIdAndIdempotencyKeyForUpdate(UUID userId, String idempotencyKey) {
        return orderJpaRepository.findByUserIdAndIdempotencyKeyForUpdate(userId, idempotencyKey)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        return orderJpaRepository.findAllByUserIdAndUser_DeletedAtIsNull(userId).stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Order> findPageByUserId(UUID userId, int page, int size) {
        var resultPage = orderJpaRepository.findPageIdsByUserIdAndUser_DeletedAtIsNullOrderByCreatedAtDesc(
                userId,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                loadOrdersInOrder(resultPage.getContent()),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Map<UUID, Long> countDeliveredQuantityByBookIds(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        return orderJpaRepository.countDeliveredQuantityByBookIds(bookIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public BigDecimal sumDeliveredRevenueBetween(Instant fromInclusive, Instant toExclusive) {
        return orderJpaRepository.sumDeliveredRevenueBetween(fromInclusive, toExclusive);
    }

    @Override
    public long countCreatedBetween(Instant fromInclusive, Instant toExclusive) {
        return orderJpaRepository.countCreatedBetween(fromInclusive, toExclusive);
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderJpaRepository.countByStatus(status);
    }

    @Override
    public List<RevenueChartResult> findRevenueStatsGroupByDay(Instant fromInclusive, Instant toExclusive) {
        return orderJpaRepository.findRevenueStatsGroupByDay(fromInclusive, toExclusive).stream()
                .map(row -> new RevenueChartResult(
                        row.getPeriodKey(),
                        row.getRevenue(),
                        defaultLong(row.getOrderCount())
                ))
                .toList();
    }

    @Override
    public List<RevenueChartResult> findRevenueStatsGroupByMonth(Instant fromInclusive, Instant toExclusive) {
        return orderJpaRepository.findRevenueStatsGroupByMonth(fromInclusive, toExclusive).stream()
                .map(row -> new RevenueChartResult(
                        row.getPeriodKey(),
                        row.getRevenue(),
                        defaultLong(row.getOrderCount())
                ))
                .toList();
    }

    @Override
    public List<OrderReportRowResult> findOrderReports(
            Instant fromInclusive,
            Instant toExclusive,
            OrderStatus status
    ) {
        return orderJpaRepository.findOrderReports(fromInclusive, toExclusive, status).stream()
                .map(row -> new OrderReportRowResult(
                        row.getOrderId(),
                        row.getOrderCode(),
                        row.getCustomerName(),
                        row.getStatus(),
                        row.getPaymentStatus(),
                        row.getFinalAmount(),
                        row.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<RevenueReportRowResult> findDailyRevenueReports(Instant fromInclusive, Instant toExclusive) {
        return orderJpaRepository.findDailyRevenueReports(fromInclusive, toExclusive).stream()
                .map(row -> new RevenueReportRowResult(
                        LocalDate.parse(row.getPeriodKey()),
                        defaultLong(row.getTotalOrders()),
                        row.getRevenue(),
                        defaultLong(row.getCancelledOrders())
                ))
                .toList();
    }

    @Override
    public List<TopBookStatsResult> findTopSellingBooks(int limit) {
        return orderJpaRepository.findTopSellingBooks(PageRequest.of(0, limit)).stream()
                .map(row -> new TopBookStatsResult(
                        row.getBookId(),
                        row.getTitle(),
                        defaultLong(row.getSoldQuantity()),
                        row.getRevenue()
                ))
                .toList();
    }

    @Override
    public List<OrderStatusStatsResult> countOrdersByStatus() {
        return orderJpaRepository.countOrdersByStatus().stream()
                .map(row -> new OrderStatusStatsResult(
                        row.getStatus(),
                        defaultLong(row.getCount())
                ))
                .toList();
    }

    @Override
    public List<RecentOrderResult> findRecentOrders(int limit) {
        return orderJpaRepository.findRecentOrders(PageRequest.of(0, limit)).stream()
                .map(row -> new RecentOrderResult(
                        row.getOrderId(),
                        row.getOrderCode(),
                        row.getCustomerName(),
                        row.getFinalAmount(),
                        row.getStatus(),
                        row.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAllByUser_DeletedAtIsNull().stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Order> findPageAll(int page, int size) {
        var resultPage = orderJpaRepository.findPageIdsByUser_DeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new PageSliceResult<>(
                loadOrdersInOrder(resultPage.getContent()),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderJpaRepository.findByIdAndUser_DeletedAtIsNull(order.getId())
                .orElseGet(OrderJpaEntity::new);

        UserJpaEntity user = userJpaRepository.getReferenceById(order.getUserId());
        CouponJpaEntity bookCoupon = order.getBookCouponId() != null
                ? couponJpaRepository.getReferenceById(order.getBookCouponId())
                : null;
        CouponJpaEntity shippingCoupon = order.getShippingCouponId() != null
                ? couponJpaRepository.getReferenceById(order.getShippingCouponId())
                : null;
        Map<UUID, BookJpaEntity> bookMap = order.getItems().stream()
                .map(item -> item.getBookId())
                .distinct()
                .collect(Collectors.toMap(
                        bookId -> bookId,
                        bookJpaRepository::getReferenceById
                ));
        Map<UUID, DigitalAssetJpaEntity> digitalAssetMap = order.getItems().stream()
                .map(item -> item.getDigitalAssetId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        digitalAssetId -> digitalAssetId,
                        digitalAssetJpaRepository::getReferenceById
                ));

        orderPersistenceMapper.copyToEntityWithReferences(
                order,
                entity,
                user,
                bookCoupon,
                shippingCoupon,
                bookMap,
                digitalAssetMap
        );
        return orderPersistenceMapper.toDomain(orderJpaRepository.save(entity));
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private List<Order> loadOrdersInOrder(Collection<UUID> orderIds) {
        List<UUID> orderedIds = orderIds == null
                ? List.of()
                : orderIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, OrderJpaEntity> ordersById = orderJpaRepository.findAllByIdInAndUser_DeletedAtIsNull(orderedIds).stream()
                .collect(Collectors.toMap(OrderJpaEntity::getId, Function.identity()));

        return orderedIds.stream()
                .map(ordersById::get)
                .filter(Objects::nonNull)
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }
}
