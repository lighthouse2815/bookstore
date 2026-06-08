package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, UUID> {

    @Query("""
            select c
            from CouponJpaEntity c
            where c.deletedAt is null
            order by c.createdAt desc
            """)
    List<CouponJpaEntity> findAllActive();

    @Query("""
            select c
            from CouponJpaEntity c
            where c.deletedAt is null
              and c.id = :couponId
            """)
    Optional<CouponJpaEntity> findByIdActive(@Param("couponId") UUID couponId);

    @Query("""
            select c
            from CouponJpaEntity c
            where c.id = :couponId
            """)
    Optional<CouponJpaEntity> findByIdIncludingDeleted(@Param("couponId") UUID couponId);

    @Query("""
            select c
            from CouponJpaEntity c
            where c.deletedAt is null
              and c.code = :code
            """)
    Optional<CouponJpaEntity> findByCodeActive(@Param("code") String code);

    @Query("""
            select case when count(c) > 0 then true else false end
            from CouponJpaEntity c
            where c.code = :code
            """)
    boolean existsByCodeIncludingDeleted(@Param("code") String code);
}
