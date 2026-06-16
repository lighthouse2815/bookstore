package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponUsageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CouponUsagePersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponUsageJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponUsageRepositoryAdapter implements ICouponUsageRepository {

    private final CouponUsageJpaRepository couponUsageJpaRepository;
    private final CouponUsagePersistenceMapper couponUsagePersistenceMapper;
    private final CouponJpaRepository couponJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public CouponUsage save(CouponUsage couponUsage) {
        CouponUsageJpaEntity entity = couponUsageJpaRepository.findById(couponUsage.getId())
                .orElseGet(CouponUsageJpaEntity::new);
        
        CouponJpaEntity coupon = couponJpaRepository.getReferenceById(couponUsage.getCouponId());
        UserJpaEntity user = userJpaRepository.getReferenceById(couponUsage.getUserId());
        OrderJpaEntity order = orderJpaRepository.getReferenceById(couponUsage.getOrderId());
        
        couponUsagePersistenceMapper.copyToEntity(couponUsage, entity, coupon, user, order);
        return couponUsagePersistenceMapper.toDomain(couponUsageJpaRepository.save(entity));
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        couponUsageJpaRepository.deleteByOrderId(orderId);
    }
}
