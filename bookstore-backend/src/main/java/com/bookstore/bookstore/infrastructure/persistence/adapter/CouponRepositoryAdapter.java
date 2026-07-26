package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CouponPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
    public PageSliceResult<Coupon> findPageActive(int page, int size) {
        var result = couponJpaRepository.findAllByDeletedAtIsNull(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return new PageSliceResult<>(
                result.getContent().stream().map(couponPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                page,
                size
        );
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
    public Optional<Coupon> findByIdIncludingDeletedForUpdate(UUID couponId) {
        return couponJpaRepository.findByIdForUpdate(couponId)
                .map(couponPersistenceMapper::toDomain);
    }



    @Override
    public Optional<Coupon> findByCodeActive(String code) {
        return couponJpaRepository.findByDeletedAtIsNullAndCode(code)
                .map(couponPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByCodeActiveForUpdate(String code) {
        return couponJpaRepository.findByDeletedAtIsNullAndCodeForUpdate(code)
                .map(couponPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByCodeIncludingDeleted(String code) {
        return couponJpaRepository.existsByCode(code);
    }

    @Override
    public long countActiveCouponsAt(Instant at) {
        return couponJpaRepository.countActiveCouponsAt(at);
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponJpaEntity entity = couponJpaRepository.findById(coupon.getId())
                .orElseGet(CouponJpaEntity::new);

        couponPersistenceMapper.copyToEntity(coupon, entity);
        return couponPersistenceMapper.toDomain(couponJpaRepository.save(entity));
    }
}
