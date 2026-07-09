package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.time.Instant;
import java.util.UUID;

public interface IAuditLogService {

    AuditLogResult record(AuditLogCommand command);

    AuditLogResult recordCreate(AuditLogCommand command);

    AuditLogResult recordUpdate(AuditLogCommand command);

    AuditLogResult recordDelete(AuditLogCommand command);

    AuditLogResult recordStatusChange(AuditLogCommand command);

    PageSliceResult<AuditLogResult> getAll(
            int page,
            int size,
            String action,
            String targetType,
            UUID actorId,
            Instant from,
            Instant to
    );

    AuditLogResult getById(UUID auditLogId);
}
