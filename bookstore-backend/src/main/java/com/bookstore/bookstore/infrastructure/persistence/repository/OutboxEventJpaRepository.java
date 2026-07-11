package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.OutboxEventJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    Optional<OutboxEventJpaEntity> findByDeduplicationKey(String deduplicationKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e from OutboxEventJpaEntity e
            where e.status in :statuses and e.nextAttemptAt <= :now
            order by e.nextAttemptAt asc, e.createdAt asc, e.id asc
            """)
    List<OutboxEventJpaEntity> findClaimableForUpdate(
            @Param("statuses") Collection<OutboxStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e from OutboxEventJpaEntity e
            where e.status = :status and e.lockedAt is not null and e.lockedAt <= :lockedBefore
            order by e.lockedAt asc, e.id asc
            """)
    List<OutboxEventJpaEntity> findStaleProcessingForUpdate(
            @Param("status") OutboxStatus status,
            @Param("lockedBefore") Instant lockedBefore,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEventJpaEntity e where e.id = :id")
    Optional<OutboxEventJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    Page<OutboxEventJpaEntity> findAllByStatusOrderByCreatedAtDesc(OutboxStatus status, Pageable pageable);

    long countByStatus(OutboxStatus status);
}
