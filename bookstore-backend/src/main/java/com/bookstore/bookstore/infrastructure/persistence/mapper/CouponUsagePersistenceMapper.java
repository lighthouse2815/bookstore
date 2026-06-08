package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponUsageJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponUsagePersistenceMapper {

    public CouponUsage toDomain(CouponUsageJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CouponUsage(
                entity.getId(),
                entity.getCouponId(),
                entity.getUserId(),
                entity.getOrderId(),
                entity.getUsedAt()
        );
    }

    public void copyToEntity(CouponUsage couponUsage, CouponUsageJpaEntity entity) {
        entity.setId(couponUsage.getId());
        entity.setCouponId(couponUsage.getCouponId());
        entity.setUserId(couponUsage.getUserId());
        entity.setOrderId(couponUsage.getOrderId());
        entity.setUsedAt(couponUsage.getUsedAt());
    }
}
