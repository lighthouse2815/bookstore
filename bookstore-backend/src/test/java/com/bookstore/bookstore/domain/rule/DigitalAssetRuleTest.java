package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DigitalAssetRuleTest {

    @Test
    void requireActive_rejectsDeletedAsset() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> DigitalAssetRule.requireActive(Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.DIGITAL_ASSET_ALREADY_DELETED, exception.getErrorCode());
    }
}
