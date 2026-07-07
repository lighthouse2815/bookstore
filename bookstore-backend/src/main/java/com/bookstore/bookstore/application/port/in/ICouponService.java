package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateCouponCommand;
import com.bookstore.bookstore.application.command.DeleteCouponCommand;
import com.bookstore.bookstore.application.command.UpdateCouponCommand;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ICouponService {

    List<Coupon> getAll();

    PageSliceResult<Coupon> getAll(int page, int size);

    List<Coupon> getPublicActivePromotions(Instant at);

    Coupon getById(UUID couponId);

    Coupon create(CreateCouponCommand command);

    Coupon update(UpdateCouponCommand command);

    void delete(DeleteCouponCommand command);
}
