package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.OtpPurpose;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserOtpJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserOtpJpaRepository extends JpaRepository<UserOtpJpaEntity, UUID> {

    Optional<UserOtpJpaEntity> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(
            UUID userId,
            OtpPurpose purpose
    );

    Optional<UserOtpJpaEntity> findFirstByUserIdAndPurposeAndVerifiedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(
            UUID userId,
            OtpPurpose purpose
    );

    Optional<UserOtpJpaEntity> findFirstByUserIdAndPurposeAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
            UUID userId,
            OtpPurpose purpose,
            Instant createdAfter
    );

    long countByUserIdAndPurposeAndCreatedAtGreaterThanEqual(
            UUID userId,
            OtpPurpose purpose,
            Instant createdAfter
    );

    @Modifying
    @Query("""
            update UserOtpJpaEntity uo
            set uo.invalidatedAt = :invalidatedAt,
                uo.updatedAt = :invalidatedAt
            where uo.user.id = :userId
              and uo.user.deletedAt is null
              and uo.purpose = :purpose
              and uo.verifiedAt is null
              and uo.invalidatedAt is null
            """)
    void invalidatePendingByUserIdAndPurpose(
            @Param("userId") UUID userId,
            @Param("purpose") OtpPurpose purpose, 
            @Param("invalidatedAt") Instant invalidatedAt
    );

    @Modifying
    @Query("""
            update UserOtpJpaEntity uo
            set uo.invalidatedAt = :invalidatedAt,
                uo.updatedAt = :invalidatedAt
            where uo.user.id = :userId
              and uo.user.deletedAt is null
              and uo.purpose = :purpose
              and uo.invalidatedAt is null
            """)
    void invalidateActiveByUserIdAndPurpose(
            @Param("userId") UUID userId,
            @Param("purpose") OtpPurpose purpose,
            @Param("invalidatedAt") Instant invalidatedAt
    );
}
