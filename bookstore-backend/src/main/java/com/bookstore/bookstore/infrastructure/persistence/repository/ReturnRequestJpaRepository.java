package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReturnRequestJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequestJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and rr.id = :requestId
            """)
    Optional<ReturnRequestJpaEntity> findByIdActive(@Param("requestId") UUID requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and rr.id = :requestId
            """)
    Optional<ReturnRequestJpaEntity> findByIdActiveForUpdate(@Param("requestId") UUID requestId);

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and rr.id = :requestId
              and rr.user.id = :userId
            """)
    Optional<ReturnRequestJpaEntity> findByIdAndUserIdActive(
            @Param("requestId") UUID requestId,
            @Param("userId") UUID userId
    );

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and rr.user.id = :userId
              and (:status is null or rr.status = :status)
              and (:orderId is null or rr.order.id = :orderId)
            order by rr.createdAt desc, rr.id desc
            """)
    List<ReturnRequestJpaEntity> findAllByUserId(
            @Param("userId") UUID userId,
            @Param("status") ReturnRequestStatus status,
            @Param("orderId") UUID orderId
    );

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and rr.user.id = :userId
              and (:status is null or rr.status = :status)
              and (:orderId is null or rr.order.id = :orderId)
            order by rr.createdAt desc, rr.id desc
            """)
    Page<ReturnRequestJpaEntity> findPageByUserId(
            @Param("userId") UUID userId,
            @Param("status") ReturnRequestStatus status,
            @Param("orderId") UUID orderId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and (:status is null or rr.status = :status)
              and (:userId is null or rr.user.id = :userId)
              and (:orderId is null or rr.order.id = :orderId)
            order by rr.createdAt desc, rr.id desc
            """)
    List<ReturnRequestJpaEntity> findAllActive(
            @Param("status") ReturnRequestStatus status,
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    @EntityGraph(attributePaths = {"order", "user", "processedByUser"})
    @Query("""
            select rr
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.user.deletedAt is null
              and (:status is null or rr.status = :status)
              and (:userId is null or rr.user.id = :userId)
              and (:orderId is null or rr.order.id = :orderId)
            order by rr.createdAt desc, rr.id desc
            """)
    Page<ReturnRequestJpaEntity> findPageActive(
            @Param("status") ReturnRequestStatus status,
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId,
            Pageable pageable
    );

    @Query("""
            select count(rr) > 0
            from ReturnRequestJpaEntity rr
            where rr.deletedAt is null
              and rr.order.id = :orderId
              and rr.status in :statuses
            """)
    boolean existsActiveByOrderIdAndStatuses(
            @Param("orderId") UUID orderId,
            @Param("statuses") Collection<ReturnRequestStatus> statuses
    );
}
