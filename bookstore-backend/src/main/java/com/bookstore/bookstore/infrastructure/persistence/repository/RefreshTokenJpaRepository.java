package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    @Query("""
            select rt
            from RefreshTokenJpaEntity rt
            where rt.token = :token
            """)
    Optional<RefreshTokenJpaEntity> findByToken(@Param("token") String token);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity rt
            set rt.revoked = true
            where rt.userId = :userId
              and rt.revoked = false
            """)
    void revokeAllByUserId(@Param("userId") UUID userId);
}
