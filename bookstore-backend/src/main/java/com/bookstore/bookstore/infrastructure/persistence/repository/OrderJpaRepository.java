package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.projection.OrderStatusStatsProjection;
import com.bookstore.bookstore.infrastructure.persistence.projection.RecentOrderProjection;
import com.bookstore.bookstore.infrastructure.persistence.projection.RevenueStatsProjection;
import com.bookstore.bookstore.infrastructure.persistence.projection.TopBookStatsProjection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;


public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderJpaEntity> findByIdAndUser_DeletedAtIsNull(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "items")
    @Query("""
            select o
            from OrderJpaEntity o
            where o.id = :id
              and o.user.deletedAt is null
            """)
    Optional<OrderJpaEntity> findByIdAndUser_DeletedAtIsNullForUpdate(@Param("id") UUID id);


    @EntityGraph(attributePaths = "items")
    List<OrderJpaEntity> findAllByUserIdAndUser_DeletedAtIsNull(UUID userId);

    @EntityGraph(attributePaths = "items")
    Page<OrderJpaEntity> findAllByUserIdAndUser_DeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);


    @Query("""
            select i.book.id, sum(i.quantity)
            from OrderJpaEntity o
            join o.items i
            where o.status = com.bookstore.bookstore.domain.enums.OrderStatus.DELIVERED
              and i.book.id in :bookIds
              and i.book.deletedAt is null
            group by i.book.id
            """)
    List<Object[]> countDeliveredQuantityByBookIds(@Param("bookIds") Collection<UUID> bookIds);

    @Query("""
            select coalesce(sum(o.finalAmount), 0)
            from OrderJpaEntity o
            where o.status = com.bookstore.bookstore.domain.enums.OrderStatus.DELIVERED
              and o.user.deletedAt is null
              and o.createdAt >= :fromInclusive
              and o.createdAt < :toExclusive
            """)
    BigDecimal sumDeliveredRevenueBetween(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    @Query("""
            select count(o)
            from OrderJpaEntity o
            where o.user.deletedAt is null
              and o.createdAt >= :fromInclusive
              and o.createdAt < :toExclusive
            """)
    long countCreatedBetween(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    @Query("""
            select count(o)
            from OrderJpaEntity o
            where o.user.deletedAt is null
              and o.status = :status
            """)
    long countByStatus(@Param("status") OrderStatus status);

    @Query(
            value = """
                    select date_format(o.created_at, '%Y-%m-%d') as periodKey,
                           coalesce(sum(o.final_amount), 0) as revenue,
                           count(o.id) as orderCount
                    from orders o
                    join users u on u.id = o.user_id
                    where o.status = 'DELIVERED'
                      and u.deleted_at is null
                      and o.created_at >= :fromInclusive
                      and o.created_at < :toExclusive
                    group by date_format(o.created_at, '%Y-%m-%d')
                    order by periodKey
                    """,
            nativeQuery = true
    )
    List<RevenueStatsProjection> findRevenueStatsGroupByDay(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    @Query(
            value = """
                    select date_format(o.created_at, '%Y-%m') as periodKey,
                           coalesce(sum(o.final_amount), 0) as revenue,
                           count(o.id) as orderCount
                    from orders o
                    join users u on u.id = o.user_id
                    where o.status = 'DELIVERED'
                      and u.deleted_at is null
                      and o.created_at >= :fromInclusive
                      and o.created_at < :toExclusive
                    group by date_format(o.created_at, '%Y-%m')
                    order by periodKey
                    """,
            nativeQuery = true
    )
    List<RevenueStatsProjection> findRevenueStatsGroupByMonth(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    @Query("""
            select i.book.id as bookId,
                   i.book.title as title,
                   sum(i.quantity) as soldQuantity,
                   coalesce(sum(i.lineTotal), 0) as revenue
            from OrderJpaEntity o
            join o.items i
            where o.status = com.bookstore.bookstore.domain.enums.OrderStatus.DELIVERED
              and o.user.deletedAt is null
              and i.book.deletedAt is null
            group by i.book.id, i.book.title
            order by sum(i.quantity) desc, sum(i.lineTotal) desc
            """)
    List<TopBookStatsProjection> findTopSellingBooks(Pageable pageable);

    @Query("""
            select o.status as status, count(o) as count
            from OrderJpaEntity o
            where o.user.deletedAt is null
            group by o.status
            order by o.status
            """)
    List<OrderStatusStatsProjection> countOrdersByStatus();

    @Query("""
            select o.id as orderId,
                   o.orderCode as orderCode,
                   o.receiverName as customerName,
                   o.finalAmount as finalAmount,
                   o.status as status,
                   o.createdAt as createdAt
            from OrderJpaEntity o
            where o.user.deletedAt is null
            order by o.createdAt desc
            """)
    List<RecentOrderProjection> findRecentOrders(Pageable pageable);

    @EntityGraph(attributePaths = "items")
    List<OrderJpaEntity> findAllByUser_DeletedAtIsNull();

    @EntityGraph(attributePaths = "items")
    Page<OrderJpaEntity> findAllByUser_DeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);


}
