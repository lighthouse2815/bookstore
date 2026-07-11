package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select prt from PasswordResetTokenJpaEntity prt join fetch prt.user where prt.tokenHash = :tokenHash")
    Optional<PasswordResetTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetTokenJpaEntity prt
            set prt.usedAt = :usedAt
            where prt.user.id = :userId
              and prt.user.deletedAt is null
              and prt.usedAt is null
            """)
    void markUnusedByUserIdAsUsed(
            @Param("userId") UUID userId,
            @Param("usedAt") Instant usedAt
    );
}
