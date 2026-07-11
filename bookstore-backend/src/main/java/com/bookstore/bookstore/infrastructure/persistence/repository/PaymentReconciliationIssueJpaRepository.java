package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentReconciliationIssueJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentReconciliationIssueJpaRepository extends JpaRepository<PaymentReconciliationIssueJpaEntity, UUID> {
    boolean existsByDeduplicationKey(String deduplicationKey);

    @Query("""
            select i from PaymentReconciliationIssueJpaEntity i
            where (:status is null or i.status = :status)
              and (:issueType is null or i.issueType = :issueType)
              and (:from is null or i.detectedAt >= :from)
              and (:to is null or i.detectedAt < :to)
            order by i.detectedAt desc, i.id desc
            """)
    Page<PaymentReconciliationIssueJpaEntity> findPage(
            @Param("status") PaymentReconciliationStatus status,
            @Param("issueType") PaymentReconciliationIssueType issueType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
