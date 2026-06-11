package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserOtpJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserOtpJpaRepository extends JpaRepository<UserOtpJpaEntity, UUID> {

    Optional<UserOtpJpaEntity> findFirstByUserIdAndVerifiedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(
            UUID userId
    );

    @Modifying
    @Query("""
            update UserOtpJpaEntity uo
            set uo.invalidatedAt = :invalidatedAt,
                uo.updatedAt = :invalidatedAt
            where uo.userId = :userId
              and uo.verifiedAt is null
              and uo.invalidatedAt is null
            """)
    void invalidatePendingByUserId(
            @Param("userId") UUID userId,
            @Param("invalidatedAt") Instant invalidatedAt
    );
}
