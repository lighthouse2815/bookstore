package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class CouponRule {

    private CouponRule() {
    }

    public static void requireValidDiscountValue(CouponDiscountType discountType, BigDecimal discountValue) {
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE, "discountValue");
        }

        if (discountType == CouponDiscountType.PERCENTAGE
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE, "discountValue");
        }
    }

    public static void requireNonNegativeMinOrderAmount(BigDecimal minOrderAmount) {
        if (minOrderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_MIN_ORDER_AMOUNT, "minOrderAmount");
        }
    }

    public static void requireNonNegativeDiscountableAmount(BigDecimal discountableAmount) {
        if (discountableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_DISCOUNT_VALUE, "discountableAmount");
        }
    }

    public static void requireNonNegativeMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_MAX_DISCOUNT_AMOUNT, "maxDiscountAmount");
        }
    }

    public static void requirePositiveMaxUsageCount(Integer maxUsageCount) {
        if (maxUsageCount != null && maxUsageCount <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_MAX_USAGE_COUNT, "maxUsageCount");
        }
    }

    public static void requireNonNegativeUsedCount(Integer usedCount) {
        if (usedCount < 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_USED_COUNT, "usedCount");
        }
    }

    public static void requireRollbackableUsage(Integer usedCount) {
        if (usedCount <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_USED_COUNT, "usedCount");
        }
    }

    public static void requireValidSchedule(Instant startsAt, Instant expiresAt) {
        if (startsAt != null && expiresAt != null && !expiresAt.isAfter(startsAt)) {
            throw new DomainException(DomainErrorCode.INVALID_COUPON_SCHEDULE);
        }
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentCode,
            String currentDescription,
            CouponType currentCouponType,
            CouponDiscountType currentDiscountType,
            BigDecimal currentDiscountValue,
            BigDecimal currentMinOrderAmount,
            BigDecimal currentMaxDiscountAmount,
            Integer currentMaxUsageCount,
            Instant currentStartsAt,
            Instant currentExpiresAt,
            boolean currentActive,
            String nextCode,
            String nextDescription,
            CouponType nextCouponType,
            CouponDiscountType nextDiscountType,
            BigDecimal nextDiscountValue,
            BigDecimal nextMinOrderAmount,
            BigDecimal nextMaxDiscountAmount,
            Integer nextMaxUsageCount,
            Instant nextStartsAt,
            Instant nextExpiresAt,
            boolean nextActive
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.COUPON_ALREADY_DELETED);
        }

        if (Objects.equals(currentCode, nextCode)
                && Objects.equals(currentDescription, nextDescription)
                && currentCouponType == nextCouponType
                && currentDiscountType == nextDiscountType
                && sameAmount(currentDiscountValue, nextDiscountValue)
                && sameAmount(currentMinOrderAmount, nextMinOrderAmount)
                && sameAmount(currentMaxDiscountAmount, nextMaxDiscountAmount)
                && Objects.equals(currentMaxUsageCount, nextMaxUsageCount)
                && Objects.equals(currentStartsAt, nextStartsAt)
                && Objects.equals(currentExpiresAt, nextExpiresAt)
                && currentActive == nextActive) {
            throw new DomainException(DomainErrorCode.COUPON_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.COUPON_ALREADY_DELETED);
        }
    }

    public static void requireApplicable(
            Instant deletedAt,
            boolean active,
            Instant startsAt,
            Instant expiresAt,
            BigDecimal minOrderAmount,
            Integer maxUsageCount,
            Integer usedCount,
            BigDecimal orderAmount,
            Instant now
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.COUPON_ALREADY_DELETED);
        }

        if (!active) {
            throw new DomainException(DomainErrorCode.COUPON_INACTIVE);
        }

        if (now.isBefore(startsAt)) {
            throw new DomainException(DomainErrorCode.COUPON_NOT_STARTED);
        }

        if (now.isAfter(expiresAt)) {
            throw new DomainException(DomainErrorCode.COUPON_EXPIRED);
        }

        if (orderAmount.compareTo(minOrderAmount) < 0) {
            throw new DomainException(DomainErrorCode.COUPON_MIN_ORDER_AMOUNT_NOT_REACHED);
        }

        if (maxUsageCount != null && usedCount >= maxUsageCount) {
            throw new DomainException(DomainErrorCode.COUPON_USAGE_LIMIT_REACHED);
        }
    }

    private static boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }
}
