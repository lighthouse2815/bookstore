package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Coupon;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICouponRepository {

    List<Coupon> findAllActive();

    Optional<Coupon> findByIdActive(UUID couponId);

    Optional<Coupon> findByIdIncludingDeleted(UUID couponId);

    Optional<Coupon> findByCodeActive(String code);

    boolean existsByCodeIncludingDeleted(String code);

    long countActiveCouponsAt(Instant at);

    Coupon save(Coupon coupon);
}
