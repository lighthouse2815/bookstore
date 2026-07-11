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
    private String orderCode;
    private UUID userId;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal productTotal;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal couponDiscount;
    private BigDecimal finalAmount;
    private UUID bookCouponId;
    private String bookCouponCode;
    private UUID shippingCouponId;
    private String shippingCouponCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
    private String idempotencyKey;
    private String checkoutFingerprint;

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
        this(
                id,
                id == null ? null : id.toString(),
                userId,
                items,
                totalAmount,
                shippingFee,
                BigDecimal.ZERO,
                discountAmount,
                finalAmount,
                couponId,
                couponCode,
                null,
                null,
                paymentMethod,
                paymentStatus,
                status,
                receiverName,
                receiverPhone,
                receiverAddress,
                createdAt,
                updatedAt,
                cancelledAt
        );
    }

    public Order(
            UUID id,
            String orderCode,
            UUID userId,
            List<OrderItem> items,
            BigDecimal productTotal,
            BigDecimal shippingFee,
            BigDecimal shippingDiscount,
            BigDecimal couponDiscount,
            BigDecimal totalAmount,
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
        this(
                id,
                orderCode,
                userId,
                items,
                productTotal,
                shippingFee,
                shippingDiscount,
                couponDiscount,
                totalAmount,
                couponId,
                couponCode,
                null,
                null,
                paymentMethod,
                paymentStatus,
                status,
                receiverName,
                receiverPhone,
                receiverAddress,
                createdAt,
                updatedAt,
                cancelledAt
        );
    }

    public Order(
            UUID id,
            String orderCode,
            UUID userId,
            List<OrderItem> items,
            BigDecimal productTotal,
            BigDecimal shippingFee,
            BigDecimal shippingDiscount,
            BigDecimal couponDiscount,
            BigDecimal totalAmount,
            UUID bookCouponId,
            String bookCouponCode,
            UUID shippingCouponId,
            String shippingCouponCode,
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
        setOrderCode(orderCode);
        setUserId(userId);
        setItems(items);
        setProductTotal(productTotal);
        setShippingFee(shippingFee);
        setShippingDiscount(shippingDiscount);
        setCouponDiscount(couponDiscount);
        setTotalAmount(totalAmount);
        setBookCouponId(bookCouponId);
        setBookCouponCode(bookCouponCode);
        setShippingCouponId(shippingCouponId);
        setShippingCouponCode(shippingCouponCode);
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

    public Order(
            UUID id,
            String orderCode,
            UUID userId,
            List<OrderItem> items,
            BigDecimal productTotal,
            BigDecimal shippingFee,
            BigDecimal shippingDiscount,
            BigDecimal couponDiscount,
            BigDecimal totalAmount,
            UUID bookCouponId,
            String bookCouponCode,
            UUID shippingCouponId,
            String shippingCouponCode,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus status,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            Instant createdAt,
            Instant updatedAt,
            Instant cancelledAt,
            String idempotencyKey,
            String checkoutFingerprint
    ) {
        this(
                id,
                orderCode,
                userId,
                items,
                productTotal,
                shippingFee,
                shippingDiscount,
                couponDiscount,
                totalAmount,
                bookCouponId,
                bookCouponCode,
                shippingCouponId,
                shippingCouponCode,
                paymentMethod,
                paymentStatus,
                status,
                receiverName,
                receiverPhone,
                receiverAddress,
                createdAt,
                updatedAt,
                cancelledAt
        );
        setIdempotencyKey(idempotencyKey);
        setCheckoutFingerprint(checkoutFingerprint);
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
        cancel(Instant.now());
    }

    public void cancel(Instant cancelledAt) {
        OrderRule.requireCanCancel(status);
        Instant timestamp = Guard.notInFuture(
                cancelledAt,
                DomainErrorCode.INVALID_ORDER_CANCELLED_AT,
                "cancelledAt"
        );
        setStatus(OrderStatus.CANCELLED);
        setUpdatedAt(timestamp);
        setCancelledAt(timestamp);
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

    public void markPaymentPaid(Instant updatedAt) {
        setPaymentStatus(PaymentStatus.PAID);
        setUpdatedAt(updatedAt);
    }

    public void markPaymentCancelled(Instant updatedAt) {
        markPaymentTerminal(PaymentStatus.CANCELLED, updatedAt);
    }

    public void markPaymentExpired(Instant updatedAt) {
        markPaymentTerminal(PaymentStatus.EXPIRED, updatedAt);
    }

    private void markPaymentTerminal(PaymentStatus nextPaymentStatus, Instant updatedAt) {
        if (paymentStatus != PaymentStatus.PENDING) {
            throw new DomainException(
                    DomainErrorCode.INVALID_ORDER_PAYMENT_STATUS,
                    paymentStatus,
                    nextPaymentStatus
            );
        }
        setPaymentStatus(nextPaymentStatus);
        setUpdatedAt(updatedAt);
    }

    public UUID getCouponId() {
        return bookCouponId;
    }

    public String getCouponCode() {
        return bookCouponCode;
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_ORDER_USER_ID, "userId");
    }

    private void setOrderCode(String orderCode) {
        this.orderCode = Guard.notBlank(
                orderCode,
                DomainErrorCode.INVALID_ORDER_ORDER_CODE,
                "orderCode"
        );
    }

    private void setItems(List<OrderItem> items) {
        List<OrderItem> validItems = new ArrayList<>(
                Guard.noNullElements(items, DomainErrorCode.INVALID_ORDER_ITEMS, "items")
        );
        OrderRule.requireHasItems(validItems);
        this.items = validItems;
    }

    private void setProductTotal(BigDecimal productTotal) {
        BigDecimal validProductTotal = Guard.notNull(
                productTotal,
                DomainErrorCode.INVALID_ORDER_PRODUCT_TOTAL,
                "productTotal"
        );
        OrderRule.requireNonNegativeProductTotal(validProductTotal);
        OrderRule.requireMatchingProductTotal(items, validProductTotal);
        this.productTotal = validProductTotal;
    }

    private void setTotalAmount(BigDecimal totalAmount) {
        BigDecimal validTotalAmount = Guard.notNull(
                totalAmount,
                DomainErrorCode.INVALID_ORDER_TOTAL_AMOUNT,
                "totalAmount"
        );
        OrderRule.requireNonNegativeTotalAmount(validTotalAmount);
        OrderRule.requireMatchingTotalAmount(
                productTotal,
                shippingFee,
                shippingDiscount,
                couponDiscount,
                validTotalAmount
        );
        this.totalAmount = validTotalAmount;
        this.discountAmount = couponDiscount.add(shippingDiscount);
        this.finalAmount = validTotalAmount;
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

    private void setShippingDiscount(BigDecimal shippingDiscount) {
        BigDecimal validShippingDiscount = Guard.notNull(
                shippingDiscount,
                DomainErrorCode.INVALID_ORDER_SHIPPING_DISCOUNT,
                "shippingDiscount"
        );
        OrderRule.requireNonNegativeShippingDiscount(validShippingDiscount);
        this.shippingDiscount = validShippingDiscount;
    }

    private void setCouponDiscount(BigDecimal couponDiscount) {
        BigDecimal validCouponDiscount = Guard.notNull(
                couponDiscount,
                DomainErrorCode.INVALID_ORDER_COUPON_DISCOUNT,
                "couponDiscount"
        );
        OrderRule.requireNonNegativeCouponDiscount(validCouponDiscount);
        this.couponDiscount = validCouponDiscount;
    }

    private void setBookCouponId(UUID bookCouponId) {
        this.bookCouponId = bookCouponId;
    }

    private void setBookCouponCode(String bookCouponCode) {
        this.bookCouponCode = Guard.notBlankOrNull(
                bookCouponCode,
                DomainErrorCode.INVALID_ORDER_COUPON_CODE,
                "bookCouponCode"
        );
    }

    private void setShippingCouponId(UUID shippingCouponId) {
        this.shippingCouponId = shippingCouponId;
    }

    private void setShippingCouponCode(String shippingCouponCode) {
        this.shippingCouponCode = Guard.notBlankOrNull(
                shippingCouponCode,
                DomainErrorCode.INVALID_ORDER_COUPON_CODE,
                "shippingCouponCode"
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

    private void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = Guard.notBlankOrNull(
                idempotencyKey,
                DomainErrorCode.INVALID_ORDER_IDEMPOTENCY_KEY,
                "idempotencyKey"
        );
    }

    private void setCheckoutFingerprint(String checkoutFingerprint) {
        this.checkoutFingerprint = Guard.notBlankOrNull(
                checkoutFingerprint,
                DomainErrorCode.INVALID_ORDER_CHECKOUT_FINGERPRINT,
                "checkoutFingerprint"
        );
    }
}
