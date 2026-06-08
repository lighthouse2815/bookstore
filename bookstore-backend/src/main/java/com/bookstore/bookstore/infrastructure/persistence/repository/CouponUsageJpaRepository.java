package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CouponUsageJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsageJpaEntity, UUID> {

    void deleteByOrderId(UUID orderId);
}
