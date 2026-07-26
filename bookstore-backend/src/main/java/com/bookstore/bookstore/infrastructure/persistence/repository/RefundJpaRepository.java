package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefundJpaEntity;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefundJpaEntity r where r.id = :refundId")
    Optional<RefundJpaEntity> findByIdForUpdate(@Param("refundId") UUID refundId);

    Optional<RefundJpaEntity> findByOrderIdAndIdempotencyKey(UUID orderId, String idempotencyKey);

    @Query("""
            select r from RefundJpaEntity r
            where (:status is null or r.status = :status)
              and (:method is null or r.method = :method)
              and (:from is null or r.requestedAt >= :from)
              and (:to is null or r.requestedAt < :to)
            order by r.requestedAt desc, r.id desc
            """)
    Page<RefundJpaEntity> findPage(
            @Param("status") RefundStatus status,
            @Param("method") RefundMethod method,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
            select coalesce(sum(r.amount), 0) from RefundJpaEntity r
            where r.paymentId = :paymentId and r.status in :statuses
            """)
    BigDecimal sumAmountByPaymentIdAndStatuses(
            @Param("paymentId") UUID paymentId,
            @Param("statuses") Collection<RefundStatus> statuses
    );
}
