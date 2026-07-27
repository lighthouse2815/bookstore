package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;

public final class DigitalAssetRule {

    private DigitalAssetRule() {
    }

    public static void requireActive(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DIGITAL_ASSET_ALREADY_DELETED);
        }
    }

    public static void requireNonNegativePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(DomainErrorCode.INVALID_DIGITAL_ASSET_PRICE, "price");
        }
    }
}
