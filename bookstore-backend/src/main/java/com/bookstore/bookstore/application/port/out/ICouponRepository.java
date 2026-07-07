package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICouponRepository {

    List<Coupon> findAllActive();

    PageSliceResult<Coupon> findPageActive(int page, int size);

    Optional<Coupon> findByIdActive(UUID couponId);

    Optional<Coupon> findByIdIncludingDeleted(UUID couponId);

    Optional<Coupon> findByIdIncludingDeletedForUpdate(UUID couponId);

    Optional<Coupon> findByCodeActive(String code);

    Optional<Coupon> findByCodeActiveForUpdate(String code);

    boolean existsByCodeIncludingDeleted(String code);

    long countActiveCouponsAt(Instant at);

    Coupon save(Coupon coupon);
}
