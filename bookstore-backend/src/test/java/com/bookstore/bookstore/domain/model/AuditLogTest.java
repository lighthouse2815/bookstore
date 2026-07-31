package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void constructor_normalizesTextFields() {
        AuditLog auditLog = auditLog(
                UUID.randomUUID(),
                AuditAction.USER_CREATED,
                AuditTargetType.USER,
                Instant.EPOCH
        );

        assertEquals("admin", auditLog.getActorUsername());
        assertEquals("ADMIN", auditLog.getActorRole());
        assertEquals(AuditAction.USER_CREATED, auditLog.getAction());
        assertEquals(AuditTargetType.USER, auditLog.getTargetType());
        assertEquals("Mô tả", auditLog.getDescription());
    }

    @Test
    void constructor_whenIdIsNull_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> auditLog(null, AuditAction.USER_CREATED, AuditTargetType.USER, Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.INVALID_AUDIT_LOG_ID, exception.getErrorCode());
    }

    @Test
    void constructor_whenActionIsNull_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> auditLog(UUID.randomUUID(), null, AuditTargetType.USER, Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.INVALID_AUDIT_LOG_ACTION, exception.getErrorCode());
    }

    @Test
    void constructor_whenTargetTypeIsNull_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> auditLog(UUID.randomUUID(), AuditAction.USER_CREATED, null, Instant.EPOCH)
        );

        assertEquals(DomainErrorCode.INVALID_AUDIT_LOG_TARGET_TYPE, exception.getErrorCode());
    }

    @Test
    void constructor_whenCreatedAtIsInFuture_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> auditLog(
                        UUID.randomUUID(),
                        AuditAction.USER_CREATED,
                        AuditTargetType.USER,
                        Instant.now().plusSeconds(60)
                )
        );

        assertEquals(DomainErrorCode.INVALID_AUDIT_LOG_CREATED_AT, exception.getErrorCode());
    }

    private static AuditLog auditLog(
            UUID id,
            AuditAction action,
            AuditTargetType targetType,
            Instant createdAt
    ) {
        return new AuditLog(
                id,
                UUID.randomUUID(),
                "  admin  ",
                "  ADMIN  ",
                action,
                targetType,
                "  target-id  ",
                "  Mô tả  ",
                "  {\"before\":true}  ",
                "  {\"after\":true}  ",
                "  127.0.0.1  ",
                "  JUnit  ",
                createdAt
        );
    }
}
