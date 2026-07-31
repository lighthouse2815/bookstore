package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class FileAssetRuleTest {

    @Test
    void requireNotDeleted_rejectsDeletedStatus() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> FileAssetRule.requireNotDeleted(FileStatus.DELETED, null)
        );

        assertEquals(DomainErrorCode.FILE_ASSET_ALREADY_DELETED, exception.getErrorCode());
    }
}
