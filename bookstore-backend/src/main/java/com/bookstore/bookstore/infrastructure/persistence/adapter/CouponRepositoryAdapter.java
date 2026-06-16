package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CouponPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements ICouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponPersistenceMapper couponPersistenceMapper;

    @Override
    public List<Coupon> findAllActive() {
        return couponJpaRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(couponPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Coupon> findByIdActive(UUID couponId) {
        return couponJpaRepository.findByDeletedAtIsNullAndId(couponId)
                .map(couponPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByIdIncludingDeleted(UUID couponId) {
        return couponJpaRepository.findById(couponId)
                .map(couponPersistenceMapper::toDomain);
    }



    @Override
    public Optional<Coupon> findByCodeActive(String code) {
        return couponJpaRepository.findByDeletedAtIsNullAndCode(code)
                .map(couponPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByCodeIncludingDeleted(String code) {
        return couponJpaRepository.existsByCode(code);
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponJpaEntity entity = couponJpaRepository.findById(coupon.getId())
                .orElseGet(CouponJpaEntity::new);

        couponPersistenceMapper.copyToEntity(coupon, entity);
        return couponPersistenceMapper.toDomain(couponJpaRepository.save(entity));
    }
}
