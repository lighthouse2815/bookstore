package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.model.AuditLog;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IAuditLogRepository {

    AuditLog save(AuditLog auditLog);

    PageSliceResult<AuditLog> findPage(
            int page,
            int size,
            String action,
            AuditTargetType targetType,
            UUID actorId,
            Instant from,
            Instant to
    );

    Optional<AuditLog> findById(UUID auditLogId);
}
