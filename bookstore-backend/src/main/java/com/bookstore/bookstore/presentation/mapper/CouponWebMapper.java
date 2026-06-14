package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateCouponCommand;
import com.bookstore.bookstore.application.command.DeleteCouponCommand;
import com.bookstore.bookstore.application.command.UpdateCouponCommand;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.presentation.request.CreateCouponRequest;
import com.bookstore.bookstore.presentation.request.UpdateCouponRequest;
import com.bookstore.bookstore.presentation.response.CouponResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CouponWebMapper {

    public CreateCouponCommand toCreateCommand(CreateCouponRequest request) {
        return new CreateCouponCommand(
                request.code(),
                request.description(),
                request.couponType(),
                request.discountType(),
                request.discountValue(),
                request.minOrderAmount(),
                request.maxDiscountAmount(),
                request.maxUsageCount(),
                request.startsAt(),
                request.expiresAt(),
                request.active()
        );
    }

    public UpdateCouponCommand toUpdateCommand(UUID couponId, UpdateCouponRequest request) {
        return new UpdateCouponCommand(
                couponId,
                request.code(),
                request.description(),
                request.couponType(),
                request.discountType(),
                request.discountValue(),
                request.minOrderAmount(),
                request.maxDiscountAmount(),
                request.maxUsageCount(),
                request.startsAt(),
                request.expiresAt(),
                request.active()
        );
    }

    public DeleteCouponCommand toDeleteCommand(UUID couponId) {
        return new DeleteCouponCommand(couponId);
    }

    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getCouponType(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                coupon.getMaxUsageCount(),
                coupon.getUsedCount(),
                coupon.getStartsAt(),
                coupon.getExpiresAt(),
                coupon.isActive(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
