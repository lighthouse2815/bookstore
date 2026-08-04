package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.AuditLogAssembler;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.out.IAuditLogRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.model.AuditLog;
import com.bookstore.bookstore.shared.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService implements IAuditLogService {

    private static final String REDACTED_VALUE = "***REDACTED***";
    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "password",
            "newpassword",
            "passwordhash",
            "token",
            "accesstoken",
            "refreshtoken",
            "resettoken",
            "idtoken",
            "tokenhash",
            "unsubscribetoken",
            "apitoken",
            "otpcode",
            "otphash",
            "secret",
            "secretkey",
            "authorizationheader",
            "apikey",
            "webhookapikey",
            "accesskey",
            "secretkeyheader"
    );

    private final IAuditLogRepository auditLogRepository;
    private final AuditLogAssembler auditLogAssembler;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public AuditLogResult record(AuditLogCommand command) {
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordCreate(AuditLogCommand command) {
        requirePayload(command, false, true);
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordUpdate(AuditLogCommand command) {
        requirePayload(command, true, true);
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordDelete(AuditLogCommand command) {
        requirePayload(command, true, false);
        return persist(command);
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<AuditLogResult> getAll(
            PageQuery pageQuery,
            AuditAction action,
            AuditTargetType targetType,
            UUID actorId,
            Instant from,
            Instant to
    ) {
        return auditLogRepository.findPage(
                        pageQuery.page(),
                        pageQuery.size(),
                        action,
                        targetType,
                        actorId,
                        from,
                        to
                )
                .map(auditLogAssembler::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResult getById(UUID auditLogId) {
        if (auditLogId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "auditLogId");
        }

        return auditLogRepository.findById(auditLogId)
                .map(auditLogAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUDIT_LOG_NOT_FOUND));
    }

    private AuditLogResult persist(AuditLogCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant createdAt = command.createdAt() == null ? Instant.now() : command.createdAt();
        AuditLog auditLog = new AuditLog(
                UUID.randomUUID(),
                command.actorId(),
                truncate(command.actorUsername(), 100),
                truncate(command.actorRole(), 50),
                command.action(),
                command.targetType(),
                truncate(command.targetId(), 100),
                truncate(command.description(), 500),
                toSanitizedJson(command.beforeValue()),
                toSanitizedJson(command.afterValue()),
                truncate(command.ipAddress(), 100),
                truncate(command.userAgent(), 500),
                createdAt
        );

        return auditLogAssembler.toResult(auditLogRepository.save(auditLog));
    }

    private void requirePayload(AuditLogCommand command, boolean requireBeforeValue, boolean requireAfterValue) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }
        if (requireBeforeValue && command.beforeValue() == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "beforeValue");
        }
        if (requireAfterValue && command.afterValue() == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "afterValue");
        }
    }

    private String truncate(String value, int maxLength) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private String toSanitizedJson(Object value) {
        if (value == null) {
            return null;
        }

        JsonNode node = objectMapper.valueToTree(value);
        sanitizeNode(node);
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "auditLogPayload");
        }
    }

    private void sanitizeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node instanceof ObjectNode objectNode) {
            var fields = objectNode.properties().iterator();
            while (fields.hasNext()) {
                var entry = fields.next();
                if (isSensitiveField(entry.getKey())) {
                    objectNode.put(entry.getKey(), REDACTED_VALUE);
                    continue;
                }
                sanitizeNode(entry.getValue());
            }
            return;
        }

        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode child : arrayNode) {
                sanitizeNode(child);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        return fieldName != null
                && SENSITIVE_FIELD_NAMES.contains(fieldName.toLowerCase(Locale.ROOT));
    }

}
