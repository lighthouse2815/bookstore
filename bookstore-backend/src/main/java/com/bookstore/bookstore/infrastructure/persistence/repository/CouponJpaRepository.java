package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, UUID> {

    List<CouponJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<CouponJpaEntity> findByDeletedAtIsNullAndId(@Param("couponId") UUID couponId);

    Optional<CouponJpaEntity> findById(@Param("couponId") UUID couponId);

    Optional<CouponJpaEntity> findByDeletedAtIsNullAndCode(@Param("code") String code);

    boolean existsByCode(@Param("code") String code);

    @Query("""
            select count(c)
            from CouponJpaEntity c
            where c.deletedAt is null
              and c.active = true
              and c.startsAt <= :at
              and c.expiresAt > :at
              and (c.maxUsageCount is null or c.usedCount < c.maxUsageCount)
            """)
    long countActiveCouponsAt(@Param("at") Instant at);
}

