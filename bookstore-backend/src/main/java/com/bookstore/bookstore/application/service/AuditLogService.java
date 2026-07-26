package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.AuditLogAssembler;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.out.IAuditLogRepository;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.AuditLog;
import com.bookstore.bookstore.shared.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService implements IAuditLogService {

    private static final String REDACTED_VALUE = "***REDACTED***";
    private static final List<String> SENSITIVE_FIELD_MARKERS = List.of(
            "password",
            "token",
            "secret",
            "authorization",
            "api_key",
            "apikey",
            "accesskey",
            "access_key",
            "refreshkey",
            "refresh_token",
            "accesstoken",
            "refresh_token",
            "refreshToken".toLowerCase(Locale.ROOT)
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
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordUpdate(AuditLogCommand command) {
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordDelete(AuditLogCommand command) {
        return persist(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogResult recordStatusChange(AuditLogCommand command) {
        return persist(command);
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<AuditLogResult> getAll(
            int page,
            int size,
            String action,
            String targetType,
            UUID actorId,
            Instant from,
            Instant to
    ) {
        validatePageRequest(page, size);
        return auditLogRepository.findPage(page, size, action, targetType, actorId, from, to)
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
                requireValue(command.action(), "action", 100),
                requireValue(command.targetType(), "targetType", 100),
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

    private String requireValue(String value, String argumentName, int maxLength) {
        String normalized = truncate(value, maxLength);
        if (normalized == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, argumentName);
        }
        return normalized;
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
        String normalized = fieldName == null ? "" : fieldName.replace("-", "").replace(" ", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_MARKERS.stream()
                .map(marker -> marker.replace("-", "").replace(" ", "").toLowerCase(Locale.ROOT))
                .anyMatch(normalized::contains);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }
}
