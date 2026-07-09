package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.AuditLog;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceMapper {

    public AuditLog toDomain(AuditLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AuditLog(
                entity.getId(),
                entity.getActorId(),
                entity.getActorUsername(),
                entity.getActorRole(),
                entity.getAction(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getDescription(),
                entity.getBeforeValue(),
                entity.getAfterValue(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(AuditLogJpaEntity entity, AuditLog auditLog) {
        entity.setId(auditLog.getId());
        entity.setActorId(auditLog.getActorId());
        entity.setActorUsername(auditLog.getActorUsername());
        entity.setActorRole(auditLog.getActorRole());
        entity.setAction(auditLog.getAction());
        entity.setTargetType(auditLog.getTargetType());
        entity.setTargetId(auditLog.getTargetId());
        entity.setDescription(auditLog.getDescription());
        entity.setBeforeValue(auditLog.getBeforeValue());
        entity.setAfterValue(auditLog.getAfterValue());
        entity.setIpAddress(auditLog.getIpAddress());
        entity.setUserAgent(auditLog.getUserAgent());
        entity.setCreatedAt(auditLog.getCreatedAt());
    }
}
