package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponPersistenceMapper {

    public Coupon toDomain(CouponJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Coupon(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDiscountType(),
                entity.getDiscountValue(),
                entity.getMinOrderAmount(),
                entity.getMaxDiscountAmount(),
                entity.getMaxUsageCount(),
                entity.getUsedCount(),
                entity.getStartsAt(),
                entity.getExpiresAt(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(Coupon coupon, CouponJpaEntity entity) {
        entity.setId(coupon.getId());
        entity.setCode(coupon.getCode());
        entity.setDescription(coupon.getDescription());
        entity.setDiscountType(coupon.getDiscountType());
        entity.setDiscountValue(coupon.getDiscountValue());
        entity.setMinOrderAmount(coupon.getMinOrderAmount());
        entity.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
        entity.setMaxUsageCount(coupon.getMaxUsageCount());
        entity.setUsedCount(coupon.getUsedCount());
        entity.setStartsAt(coupon.getStartsAt());
        entity.setExpiresAt(coupon.getExpiresAt());
        entity.setActive(coupon.isActive());
        entity.setCreatedAt(coupon.getCreatedAt());
        entity.setUpdatedAt(coupon.getUpdatedAt());
        entity.setDeletedAt(coupon.getDeletedAt());
    }
}
