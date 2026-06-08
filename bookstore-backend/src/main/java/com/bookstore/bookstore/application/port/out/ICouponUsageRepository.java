package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.CouponUsage;
import java.util.UUID;

public interface ICouponUsageRepository {

    CouponUsage save(CouponUsage couponUsage);

    void deleteByOrderId(UUID orderId);
}
