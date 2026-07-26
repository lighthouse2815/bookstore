package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshTokenJpaEntity rt join fetch rt.user where rt.tokenHash = :tokenHash")
    Optional<RefreshTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshTokenJpaEntity rt join fetch rt.user where rt.id = :id")
    Optional<RefreshTokenJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select rt from RefreshTokenJpaEntity rt join fetch rt.user
            where rt.user.id = :userId and rt.revoked = false and rt.expiresAt > :now
            order by rt.lastUsedAt desc, rt.createdAt desc
            """)
    List<RefreshTokenJpaEntity> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity rt
            set rt.revoked = true, rt.revokedAt = :revokedAt, rt.revokeReason = :reason
            where rt.user.id = :userId
              and rt.user.deletedAt is null
              and rt.revoked = false
            """)
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt,
                           @Param("reason") com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason reason);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity rt
            set rt.revoked = true, rt.revokedAt = :revokedAt, rt.revokeReason = :reason
            where rt.familyId = :familyId and rt.revoked = false
            """)
    void revokeFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt,
                      @Param("reason") com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason reason);
}
