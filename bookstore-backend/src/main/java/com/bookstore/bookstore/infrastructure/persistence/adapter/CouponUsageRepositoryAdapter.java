package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponUsageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CouponUsagePersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponUsageJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponUsageRepositoryAdapter implements ICouponUsageRepository {

    private final CouponUsageJpaRepository couponUsageJpaRepository;
    private final CouponUsagePersistenceMapper couponUsagePersistenceMapper;

    @Override
    public CouponUsage save(CouponUsage couponUsage) {
        CouponUsageJpaEntity entity = couponUsageJpaRepository.findById(couponUsage.getId())
                .orElseGet(CouponUsageJpaEntity::new);
        couponUsagePersistenceMapper.copyToEntity(couponUsage, entity);
        return couponUsagePersistenceMapper.toDomain(couponUsageJpaRepository.save(entity));
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        couponUsageJpaRepository.deleteByOrderId(orderId);
    }
}
