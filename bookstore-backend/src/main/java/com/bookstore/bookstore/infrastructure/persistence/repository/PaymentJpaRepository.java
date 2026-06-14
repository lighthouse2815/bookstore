package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    Optional<PaymentJpaEntity> findByReferenceCode(String referenceCode);

    Optional<PaymentJpaEntity> findByTransactionId(String transactionId);

    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = com.bookstore.bookstore.domain.enums.PaymentProvider.SEPAY
              and p.status = com.bookstore.bookstore.domain.enums.PaymentStatus.PENDING
              and (p.transferContent = :orderCode or p.referenceCode = :orderCode)
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByOrderCode(@Param("orderCode") String orderCode);

    @Query("""
            select p
            from PaymentJpaEntity p
            where p.provider = com.bookstore.bookstore.domain.enums.PaymentProvider.SEPAY
              and p.status = com.bookstore.bookstore.domain.enums.PaymentStatus.PENDING
              and lower(:content) like concat('%', lower(p.transferContent), '%')
            order by p.createdAt asc
            """)
    Optional<PaymentJpaEntity> findPendingSepayByTransferContentInContent(@Param("content") String content);
}
