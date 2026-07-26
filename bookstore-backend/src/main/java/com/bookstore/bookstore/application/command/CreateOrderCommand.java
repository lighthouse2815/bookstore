package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public record CreateOrderCommand(
        UUID userId,
        List<UUID> cartItemIds,
        UUID addressId,
        ShippingMethod shippingMethod,
        PaymentMethod paymentMethod,
        String bookCouponCode,
        String shippingCouponCode,
        String note,
        String idempotencyKey
) {
    public CreateOrderCommand(
            UUID userId,
            List<UUID> cartItemIds,
            UUID addressId,
            ShippingMethod shippingMethod,
            PaymentMethod paymentMethod,
            String bookCouponCode,
            String shippingCouponCode,
            String note
    ) {
        this(
                userId,
                cartItemIds,
                addressId,
                shippingMethod,
                paymentMethod,
                bookCouponCode,
                shippingCouponCode,
                note,
                UUID.randomUUID().toString()
        );
    }

    public CreateOrderCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (shippingMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shippingMethod");
        }
        if (paymentMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "paymentMethod");
        }
        if (cartItemIds == null) {
            cartItemIds = List.of();
        } else {
            if (cartItemIds.stream().anyMatch(Objects::isNull)) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "cartItemIds");
            }
            cartItemIds = cartItemIds.stream()
                    .distinct()
                    .toList();
        }
        bookCouponCode = StringUtils.trimToNull(bookCouponCode);
        shippingCouponCode = StringUtils.trimToNull(shippingCouponCode);
        note = StringUtils.trimToNull(note);
        idempotencyKey = validateIdempotencyKey(idempotencyKey);
    }

    public String checkoutFingerprint() {
        String canonicalPayload = String.join(
                "|",
                "cartItemIds=" + cartItemIds.stream()
                        .map(UUID::toString)
                        .sorted()
                        .reduce((left, right) -> left + "," + right)
                        .orElse(""),
                "addressId=" + (addressId == null ? "" : addressId),
                "shippingMethod=" + shippingMethod.name(),
                "paymentMethod=" + paymentMethod.name(),
                "bookCouponCode=" + normalizeCouponCode(bookCouponCode),
                "shippingCouponCode=" + normalizeCouponCode(shippingCouponCode),
                "note=" + (note == null ? "" : note)
        );

        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonicalPayload.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }

    private static String validateIdempotencyKey(String value) {
        String normalizedValue = StringUtils.trimToNull(value);
        if (normalizedValue == null || normalizedValue.length() != 36) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "Idempotency-Key");
        }

        try {
            UUID parsed = UUID.fromString(normalizedValue);
            if (!parsed.toString().equalsIgnoreCase(normalizedValue)) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "Idempotency-Key");
            }
            return parsed.toString();
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "Idempotency-Key");
        }
    }

    private static String normalizeCouponCode(String couponCode) {
        return couponCode == null ? "" : couponCode.toUpperCase(java.util.Locale.ROOT);
    }
}
