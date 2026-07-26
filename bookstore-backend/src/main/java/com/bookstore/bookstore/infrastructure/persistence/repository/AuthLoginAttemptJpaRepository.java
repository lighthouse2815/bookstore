package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.AuthLoginAttemptJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthLoginAttemptJpaRepository extends JpaRepository<AuthLoginAttemptJpaEntity, UUID> {

    Optional<AuthLoginAttemptJpaEntity> findByAttemptTypeAndSubjectHash(String attemptType, String subjectHash);

    @Modifying
    @Query(value = """
            insert into auth_login_attempts
              (id, attempt_type, subject_hash, failure_count, window_started_at, last_failed_at, locked_until, created_at, updated_at)
            values
              (:id, :attemptType, :subjectHash, 1, :now, :now, :initialLockUntil, :now, :now)
            on duplicate key update
              failure_count = if(window_started_at <= :windowStart, 1, failure_count + 1),
              window_started_at = if(window_started_at <= :windowStart, :now, window_started_at),
              last_failed_at = :now,
              locked_until = if(
                if(window_started_at <= :windowStart, 1, failure_count + 1) >= :maxFailures,
                :lockUntil,
                locked_until
              ),
              updated_at = :now
            """, nativeQuery = true)
    void upsertFailure(
            @Param("id") UUID id,
            @Param("attemptType") String attemptType,
            @Param("subjectHash") String subjectHash,
            @Param("now") Instant now,
            @Param("windowStart") Instant windowStart,
            @Param("initialLockUntil") Instant initialLockUntil,
            @Param("lockUntil") Instant lockUntil,
            @Param("maxFailures") int maxFailures
    );

    @Modifying
    @Query("delete from AuthLoginAttemptJpaEntity a where a.attemptType = :attemptType and a.subjectHash = :subjectHash")
    void deleteByAttemptTypeAndSubjectHash(@Param("attemptType") String attemptType, @Param("subjectHash") String subjectHash);

    @Modifying
    @Query("delete from AuthLoginAttemptJpaEntity a where a.updatedAt < :before and (a.lockedUntil is null or a.lockedUntil < :before)")
    void deleteExpiredBefore(@Param("before") Instant before);
}
