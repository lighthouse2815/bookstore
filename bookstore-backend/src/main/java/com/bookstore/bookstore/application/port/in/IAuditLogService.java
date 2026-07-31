package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import java.time.Instant;
import java.util.UUID;

public interface IAuditLogService {

    AuditLogResult record(AuditLogCommand command);

    AuditLogResult recordCreate(AuditLogCommand command);

    AuditLogResult recordUpdate(AuditLogCommand command);

    AuditLogResult recordDelete(AuditLogCommand command);

    PageSliceResult<AuditLogResult> getAll(
            PageQuery pageQuery,
            AuditAction action,
            AuditTargetType targetType,
            UUID actorId,
            Instant from,
            Instant to
    );

    AuditLogResult getById(UUID auditLogId);
}
