package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.CouponRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Coupon {

    private UUID id;
    private String code;
    private String description;
    private CouponType couponType;
    private CouponDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer maxUsageCount;
    private Integer usedCount;
    private Instant startsAt;
    private Instant expiresAt;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Coupon(
            UUID id,
            String code,
            String description,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            Integer usedCount,
            Instant startsAt,
            Instant expiresAt,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this(
                id,
                code,
                description,
                CouponType.BOOK,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                maxUsageCount,
                usedCount,
                startsAt,
                expiresAt,
                active,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public Coupon(
            UUID id,
            String code,
            String description,
            CouponType couponType,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            Integer usedCount,
            Instant startsAt,
            Instant expiresAt,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_COUPON_ID, "id");
        setCode(code);
        setDescription(description);
        setCouponType(couponType);
        setDiscountType(discountType);
        setDiscountValue(discountValue);
        setMinOrderAmount(minOrderAmount);
        setMaxDiscountAmount(maxDiscountAmount);
        setMaxUsageCount(maxUsageCount);
        setUsedCount(usedCount);
        setStartsAt(startsAt);
        setExpiresAt(expiresAt);
        setActive(active);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateCoupon(
            String code,
            String description,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            Instant startsAt,
            Instant expiresAt,
            boolean active
    ) {
        updateCoupon(
                code,
                description,
                CouponType.BOOK,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                maxUsageCount,
                startsAt,
                expiresAt,
                active
        );
    }

    public void updateCoupon(
            String code,
            String description,
            CouponType couponType,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            Instant startsAt,
            Instant expiresAt,
            boolean active
    ) {
        CouponRule.requireCanUpdate(
                deletedAt,
                this.code,
                this.description,
                this.couponType,
                this.discountType,
                this.discountValue,
                this.minOrderAmount,
                this.maxDiscountAmount,
                this.maxUsageCount,
                this.startsAt,
                this.expiresAt,
                this.active,
                code,
                description,
                couponType,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                maxUsageCount,
                startsAt,
                expiresAt,
                active
        );

        setCode(code);
        setDescription(description);
        setCouponType(couponType);
        setDiscountType(discountType);
        setDiscountValue(discountValue);
        setMinOrderAmount(minOrderAmount);
        setMaxDiscountAmount(maxDiscountAmount);
        setMaxUsageCount(maxUsageCount);
        setStartsAt(startsAt);
        setExpiresAt(expiresAt);
        setActive(active);
        setUpdatedAt(Instant.now());
    }

    public BigDecimal applyToOrder(BigDecimal orderAmount, Instant appliedAt) {
        return applyTo(orderAmount, orderAmount, appliedAt);
    }

    public BigDecimal applyTo(BigDecimal orderAmount, BigDecimal discountableAmount, Instant appliedAt) {
        BigDecimal validOrderAmount = Guard.notNull(
                orderAmount,
                DomainErrorCode.INVALID_COUPON_MIN_ORDER_AMOUNT,
                "orderAmount"
        );
        BigDecimal validDiscountableAmount = Guard.notNull(
                discountableAmount,
                DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE,
                "discountableAmount"
        );
        CouponRule.requireNonNegativeDiscountableAmount(validDiscountableAmount);
        Instant validAppliedAt = Guard.notInFuture(
                appliedAt,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                "appliedAt"
        );

        CouponRule.requireApplicable(
                deletedAt,
                active,
                startsAt,
                expiresAt,
                minOrderAmount,
                maxUsageCount,
                usedCount,
                validOrderAmount,
                validAppliedAt
        );

        BigDecimal discountAmount = switch (discountType) {
            case PERCENTAGE -> validDiscountableAmount
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> discountValue;
        };

        if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
            discountAmount = maxDiscountAmount;
        }

        if (discountAmount.compareTo(validDiscountableAmount) > 0) {
            discountAmount = validDiscountableAmount;
        }

        setUsedCount(usedCount + 1);
        setUpdatedAt(validAppliedAt);
        return discountAmount;
    }

    public void rollbackUsage(Instant rolledBackAt) {
        Instant validRolledBackAt = Guard.notInFuture(
                rolledBackAt,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                "rolledBackAt"
        );

        CouponRule.requireRollbackableUsage(usedCount);
        setUsedCount(usedCount - 1);
        if (deletedAt == null) {
            setUpdatedAt(validRolledBackAt);
        }
    }

    public void softDelete() {
        CouponRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setActive(false);
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setCode(String code) {
        this.code = Guard.notBlank(code, DomainErrorCode.INVALID_COUPON_CODE, "code");
    }

    private void setDescription(String description) {
        this.description = Guard.notBlankOrNull(description, DomainErrorCode.INVALID_COUPON_DESCRIPTION, "description");
    }

    private void setCouponType(CouponType couponType) {
        this.couponType = Guard.notNull(couponType, DomainErrorCode.INVALID_COUPON_TYPE, "couponType");
    }

    private void setDiscountType(CouponDiscountType discountType) {
        this.discountType = Guard.notNull(
                discountType,
                DomainErrorCode.INVALID_COUPON_DISCOUNT_TYPE,
                "discountType"
        );
    }

    private void setDiscountValue(BigDecimal discountValue) {
        BigDecimal validDiscountValue = Guard.notNull(
                discountValue,
                DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE,
                "discountValue"
        );
        CouponRule.requireValidDiscountValue(discountType, validDiscountValue);
        this.discountValue = validDiscountValue;
    }

    private void setMinOrderAmount(BigDecimal minOrderAmount) {
        BigDecimal validMinOrderAmount = Guard.notNull(
                minOrderAmount,
                DomainErrorCode.INVALID_COUPON_MIN_ORDER_AMOUNT,
                "minOrderAmount"
        );
        CouponRule.requireNonNegativeMinOrderAmount(validMinOrderAmount);
        this.minOrderAmount = validMinOrderAmount;
    }

    private void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        CouponRule.requireNonNegativeMaxDiscountAmount(maxDiscountAmount);
        this.maxDiscountAmount = maxDiscountAmount;
    }

    private void setMaxUsageCount(Integer maxUsageCount) {
        CouponRule.requirePositiveMaxUsageCount(maxUsageCount);
        this.maxUsageCount = maxUsageCount;
    }

    private void setUsedCount(Integer usedCount) {
        Integer validUsedCount = Guard.notNull(
                usedCount,
                DomainErrorCode.INVALID_COUPON_USED_COUNT,
                "usedCount"
        );
        CouponRule.requireNonNegativeUsedCount(validUsedCount);
        this.usedCount = validUsedCount;
    }

    private void setStartsAt(Instant startsAt) {
        this.startsAt = Guard.notNull(startsAt, DomainErrorCode.INVALID_COUPON_STARTS_AT, "startsAt");
        CouponRule.requireValidSchedule(this.startsAt, this.expiresAt);
    }

    private void setExpiresAt(Instant expiresAt) {
        this.expiresAt = Guard.notNull(expiresAt, DomainErrorCode.INVALID_COUPON_EXPIRES_AT, "expiresAt");
        CouponRule.requireValidSchedule(this.startsAt, this.expiresAt);
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_COUPON_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_COUPON_CREATED_AT,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                DomainErrorCode.INVALID_COUPON_DELETED_AT,
                DomainErrorCode.INVALID_COUPON_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_COUPON_CREATED_AT,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                DomainErrorCode.INVALID_COUPON_DELETED_AT,
                DomainErrorCode.INVALID_COUPON_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_COUPON_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_COUPON_CREATED_AT,
                DomainErrorCode.INVALID_COUPON_UPDATED_AT,
                DomainErrorCode.INVALID_COUPON_DELETED_AT,
                DomainErrorCode.INVALID_COUPON_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
