package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    Optional<PaymentJpaEntity> findByReferenceCode(String referenceCode);

    Optional<PaymentJpaEntity> findByTransactionId(String transactionId);

    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = :provider
              and p.status = :status
              and (p.transferContent = :orderCode or p.referenceCode = :orderCode)
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByOrderCode(
            @Param("orderCode") String orderCode,
            @Param("provider") PaymentProvider provider,
            @Param("status") PaymentStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = :provider
              and p.status = :status
              and (p.transferContent = :orderCode or p.referenceCode = :orderCode)
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByOrderCodeForUpdate(
            @Param("orderCode") String orderCode,
            @Param("provider") PaymentProvider provider,
            @Param("status") PaymentStatus status
    );

    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = :provider
              and p.status = :status
              and lower(:content) like concat('%', lower(p.transferContent), '%')
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByTransferContentInContent(
            @Param("content") String content,
            @Param("provider") PaymentProvider provider,
            @Param("status") PaymentStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = :provider
              and p.status = :status
              and lower(:content) like concat('%', lower(p.transferContent), '%')
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByTransferContentInContentForUpdate(
            @Param("content") String content,
            @Param("provider") PaymentProvider provider,
            @Param("status") PaymentStatus status
    );
}
