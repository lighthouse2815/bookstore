package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CouponUsage {

    private UUID id;
    private UUID couponId;
    private UUID userId;
    private UUID orderId;
    private BigDecimal discountAmount;
    private Instant usedAt;

    public CouponUsage(
            UUID id,
            UUID couponId,
            UUID userId,
            UUID orderId,
            BigDecimal discountAmount,
            Instant usedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_COUPON_USAGE_ID, "id");
        this.couponId = Guard.notNull(couponId, DomainErrorCode.INVALID_COUPON_USAGE_COUPON_ID, "couponId");
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_COUPON_USAGE_USER_ID, "userId");
        this.orderId = Guard.notNull(orderId, DomainErrorCode.INVALID_COUPON_USAGE_ORDER_ID, "orderId");
        this.discountAmount = Guard.notNull(discountAmount, DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE, "discountAmount");
        if (this.discountAmount.signum() < 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE, "discountAmount");
        }
        this.usedAt = Guard.notInFuture(usedAt, DomainErrorCode.INVALID_COUPON_USAGE_USED_AT, "usedAt");
    }
}
