package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponUsageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponUsagePersistenceMapper {

    public CouponUsage toDomain(CouponUsageJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CouponUsage(
                entity.getId(),
                entity.getCoupon().getId(),
                entity.getUser().getId(),
                entity.getOrder().getId(),
                entity.getDiscountAmount(),
                entity.getUsedAt()
        );
    }

    public void copyToEntity(CouponUsage couponUsage, CouponUsageJpaEntity entity, CouponJpaEntity coupon, UserJpaEntity user, OrderJpaEntity order) {
        entity.setId(couponUsage.getId());
        entity.setCoupon(coupon);
        entity.setUser(user);
        entity.setOrder(order);
        entity.setDiscountAmount(couponUsage.getDiscountAmount());
        entity.setUsedAt(couponUsage.getUsedAt());
    }
}
