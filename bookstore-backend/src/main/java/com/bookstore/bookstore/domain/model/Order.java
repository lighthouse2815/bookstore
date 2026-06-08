package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.rule.OrderRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Order {

    private UUID id;
    private UUID userId;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private UUID couponId;
    private String couponCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;

    public Order(
            UUID id,
            UUID userId,
            List<OrderItem> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount,
            UUID couponId,
            String couponCode,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus status,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            Instant createdAt,
            Instant updatedAt,
            Instant cancelledAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_ORDER_ID, "id");
        setUserId(userId);
        setItems(items);
        setTotalAmount(totalAmount);
        setDiscountAmount(discountAmount);
        setShippingFee(shippingFee);
        setFinalAmount(finalAmount);
        setCouponId(couponId);
        setCouponCode(couponCode);
        setPaymentMethod(paymentMethod);
        setPaymentStatus(paymentStatus);
        setStatus(status);
        setReceiverName(receiverName);
        setReceiverPhone(receiverPhone);
        setReceiverAddress(receiverAddress);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setCancelledAt(cancelledAt);
        OrderRule.requireCancelledStateConsistent(this.status, this.cancelledAt);
    }

    public void updateStatus(OrderStatus nextStatus) {
        OrderRule.requireStatusChanged(this.status, nextStatus);

        switch (nextStatus) {
            case PENDING -> throw new DomainException(
                    DomainErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    this.status,
                    nextStatus
            );
            case CONFIRMED -> confirm();
            case SHIPPING -> startShipping();
            case DELIVERED -> markDelivered();
            case CANCELLED -> cancel();
        }
    }

    public void cancel() {
        OrderRule.requireCanCancel(status);
        Instant now = Instant.now();
        setStatus(OrderStatus.CANCELLED);
        setUpdatedAt(now);
        setCancelledAt(now);
    }

    public void confirm() {
        OrderRule.requireCanConfirm(status);
        setStatus(OrderStatus.CONFIRMED);
        setUpdatedAt(Instant.now());
    }

    public void startShipping() {
        OrderRule.requireCanStartShipping(status);
        setStatus(OrderStatus.SHIPPING);
        setUpdatedAt(Instant.now());
    }

    public void markDelivered() {
        OrderRule.requireCanMarkDelivered(status);
        setStatus(OrderStatus.DELIVERED);
        setUpdatedAt(Instant.now());
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_ORDER_USER_ID, "userId");
    }

    private void setItems(List<OrderItem> items) {
        List<OrderItem> validItems = new ArrayList<>(
                Guard.noNullElements(items, DomainErrorCode.INVALID_ORDER_ITEMS, "items")
        );
        OrderRule.requireHasItems(validItems);
        this.items = validItems;
    }

    private void setTotalAmount(BigDecimal totalAmount) {
        BigDecimal validTotalAmount = Guard.notNull(
                totalAmount,
                DomainErrorCode.INVALID_ORDER_TOTAL_AMOUNT,
                "totalAmount"
        );
        OrderRule.requireNonNegativeTotalAmount(validTotalAmount);
        OrderRule.requireMatchingTotalAmount(items, validTotalAmount);
        this.totalAmount = validTotalAmount;
    }

    private void setDiscountAmount(BigDecimal discountAmount) {
        BigDecimal validDiscountAmount = Guard.notNull(
                discountAmount,
                DomainErrorCode.INVALID_ORDER_DISCOUNT_AMOUNT,
                "discountAmount"
        );
        OrderRule.requireNonNegativeDiscountAmount(validDiscountAmount);
        this.discountAmount = validDiscountAmount;
    }

    private void setShippingFee(BigDecimal shippingFee) {
        BigDecimal validShippingFee = Guard.notNull(
                shippingFee,
                DomainErrorCode.INVALID_ORDER_SHIPPING_FEE,
                "shippingFee"
        );
        OrderRule.requireNonNegativeShippingFee(validShippingFee);
        this.shippingFee = validShippingFee;
    }

    private void setFinalAmount(BigDecimal finalAmount) {
        BigDecimal validFinalAmount = Guard.notNull(
                finalAmount,
                DomainErrorCode.INVALID_ORDER_FINAL_AMOUNT,
                "finalAmount"
        );
        OrderRule.requireNonNegativeFinalAmount(validFinalAmount);
        OrderRule.requireMatchingFinalAmount(totalAmount, discountAmount, shippingFee, validFinalAmount);
        this.finalAmount = validFinalAmount;
    }

    private void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }

    private void setCouponCode(String couponCode) {
        this.couponCode = Guard.notBlankOrNull(
                couponCode,
                DomainErrorCode.INVALID_ORDER_COUPON_CODE,
                "couponCode"
        );
    }

    private void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = Guard.notNull(
                paymentMethod,
                DomainErrorCode.INVALID_ORDER_PAYMENT_METHOD,
                "paymentMethod"
        );
    }

    private void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = Guard.notNull(
                paymentStatus,
                DomainErrorCode.INVALID_ORDER_PAYMENT_STATUS,
                "paymentStatus"
        );
    }

    private void setStatus(OrderStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_ORDER_STATUS, "status");
    }

    private void setReceiverName(String receiverName) {
        this.receiverName = Guard.notBlank(
                receiverName,
                DomainErrorCode.INVALID_ORDER_RECEIVER_NAME,
                "receiverName"
        );
    }

    private void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = Guard.phoneNumber(
                receiverPhone,
                DomainErrorCode.INVALID_ORDER_RECEIVER_PHONE,
                "receiverPhone"
        );
    }

    private void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = Guard.notBlank(
                receiverAddress,
                DomainErrorCode.INVALID_ORDER_RECEIVER_ADDRESS,
                "receiverAddress"
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_ORDER_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.cancelledAt,
                DomainErrorCode.INVALID_ORDER_CREATED_AT,
                DomainErrorCode.INVALID_ORDER_UPDATED_AT,
                DomainErrorCode.INVALID_ORDER_CANCELLED_AT,
                DomainErrorCode.INVALID_ORDER_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_ORDER_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.cancelledAt,
                DomainErrorCode.INVALID_ORDER_CREATED_AT,
                DomainErrorCode.INVALID_ORDER_UPDATED_AT,
                DomainErrorCode.INVALID_ORDER_CANCELLED_AT,
                DomainErrorCode.INVALID_ORDER_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setCancelledAt(Instant cancelledAt) {
        Instant validCancelledAt = Guard.notInFutureOrNull(
                cancelledAt,
                DomainErrorCode.INVALID_ORDER_CANCELLED_AT,
                "cancelledAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validCancelledAt,
                DomainErrorCode.INVALID_ORDER_CREATED_AT,
                DomainErrorCode.INVALID_ORDER_UPDATED_AT,
                DomainErrorCode.INVALID_ORDER_CANCELLED_AT,
                DomainErrorCode.INVALID_ORDER_AUDIT_ORDER
        );
        this.cancelledAt = validCancelledAt;
    }
}
