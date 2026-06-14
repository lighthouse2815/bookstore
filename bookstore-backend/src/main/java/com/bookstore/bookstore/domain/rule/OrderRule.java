package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderRule {

    private OrderRule() {
    }

    public static void requireHasItems(List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new DomainException(DomainErrorCode.ORDER_MUST_HAVE_AT_LEAST_ONE_ITEM);
        }
    }

    public static void requireNonNegativeTotalAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_TOTAL_AMOUNT, "totalAmount");
        }
    }

    public static void requireNonNegativeProductTotal(BigDecimal productTotal) {
        if (productTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_PRODUCT_TOTAL, "productTotal");
        }
    }

    public static void requireNonNegativeDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_DISCOUNT_AMOUNT, "discountAmount");
        }
    }

    public static void requireNonNegativeShippingFee(BigDecimal shippingFee) {
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_SHIPPING_FEE, "shippingFee");
        }
    }

    public static void requireNonNegativeShippingDiscount(BigDecimal shippingDiscount) {
        if (shippingDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_SHIPPING_DISCOUNT, "shippingDiscount");
        }
    }

    public static void requireNonNegativeCouponDiscount(BigDecimal couponDiscount) {
        if (couponDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_COUPON_DISCOUNT, "couponDiscount");
        }
    }

    public static void requireNonNegativeFinalAmount(BigDecimal finalAmount) {
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_FINAL_AMOUNT, "finalAmount");
        }
    }

    public static void requireMatchingTotalAmount(List<OrderItem> items, BigDecimal totalAmount) {
        BigDecimal expectedTotalAmount = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (expectedTotalAmount.compareTo(totalAmount) != 0) {
            throw new DomainException(DomainErrorCode.ORDER_TOTAL_AMOUNT_MISMATCH);
        }
    }

    public static void requireMatchingProductTotal(List<OrderItem> items, BigDecimal productTotal) {
        BigDecimal expectedProductTotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (expectedProductTotal.compareTo(productTotal) != 0) {
            throw new DomainException(DomainErrorCode.ORDER_TOTAL_AMOUNT_MISMATCH);
        }
    }

    public static void requireMatchingTotalAmount(
            BigDecimal productTotal,
            BigDecimal shippingFee,
            BigDecimal shippingDiscount,
            BigDecimal couponDiscount,
            BigDecimal totalAmount
    ) {
        BigDecimal expectedTotalAmount = productTotal
                .add(shippingFee)
                .subtract(shippingDiscount)
                .subtract(couponDiscount);

        if (expectedTotalAmount.compareTo(totalAmount) != 0) {
            throw new DomainException(DomainErrorCode.ORDER_PAYMENT_TOTAL_MISMATCH);
        }
    }

    public static void requireMatchingFinalAmount(
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount
    ) {
        BigDecimal expectedFinalAmount = totalAmount
                .subtract(discountAmount)
                .add(shippingFee);

        if (expectedFinalAmount.compareTo(finalAmount) != 0) {
            throw new DomainException(DomainErrorCode.ORDER_FINAL_AMOUNT_MISMATCH);
        }
    }

    public static void requireStatusChanged(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            throw new DomainException(DomainErrorCode.ORDER_STATUS_NOT_CHANGED);
        }
    }

    public static void requireCanCancel(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException(DomainErrorCode.CANCELLED_ORDER_CANNOT_BE_UPDATED);
        }
        if (status == OrderStatus.DELIVERED) {
            throw new DomainException(DomainErrorCode.DELIVERED_ORDER_CANNOT_BE_CANCELLED);
        }
    }

    public static void requireCanConfirm(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException(DomainErrorCode.CANCELLED_ORDER_CANNOT_BE_UPDATED);
        }
        if (status != OrderStatus.PENDING) {
            throw new DomainException(
                    DomainErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    status,
                    OrderStatus.CONFIRMED
            );
        }
    }

    public static void requireCanStartShipping(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException(DomainErrorCode.CANCELLED_ORDER_CANNOT_BE_UPDATED);
        }
        if (status != OrderStatus.CONFIRMED) {
            throw new DomainException(
                    DomainErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    status,
                    OrderStatus.SHIPPING
            );
        }
    }

    public static void requireCanMarkDelivered(OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            throw new DomainException(DomainErrorCode.CANCELLED_ORDER_CANNOT_BE_UPDATED);
        }
        if (status != OrderStatus.SHIPPING) {
            throw new DomainException(
                    DomainErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    status,
                    OrderStatus.DELIVERED
            );
        }
    }

    public static void requireCancelledStateConsistent(OrderStatus status, Instant cancelledAt) {
        if (status == OrderStatus.CANCELLED && cancelledAt == null) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_CANCELLED_AT, "cancelledAt");
        }

        if (status != OrderStatus.CANCELLED && cancelledAt != null) {
            throw new DomainException(DomainErrorCode.INVALID_ORDER_CANCELLED_AT, "cancelledAt");
        }
    }
}
