package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

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
