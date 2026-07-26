package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.domain.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogAssembler {

    public AuditLogResult toResult(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return new AuditLogResult(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorUsername(),
                auditLog.getActorRole(),
                auditLog.getAction(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDescription(),
                auditLog.getBeforeValue(),
                auditLog.getAfterValue(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }
}
