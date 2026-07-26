package com.bookstore.bookstore.presentation.support;

import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuditSupport {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditSupport.class);

    private final IAuditLogService auditLogService;

    public void recordCreate(
            Jwt jwt,
            HttpServletRequest request,
            String action,
            String targetType,
            Object targetId,
            String description,
            Object afterValue
    ) {
        recordQuietly(jwt, request, action, targetType, targetId, description, null, afterValue, AuditMode.CREATE);
    }

    public void recordUpdate(
            Jwt jwt,
            HttpServletRequest request,
            String action,
            String targetType,
            Object targetId,
            String description,
            Object beforeValue,
            Object afterValue
    ) {
        recordQuietly(jwt, request, action, targetType, targetId, description, beforeValue, afterValue, AuditMode.UPDATE);
    }

    public void recordDelete(
            Jwt jwt,
            HttpServletRequest request,
            String action,
            String targetType,
            Object targetId,
            String description,
            Object beforeValue
    ) {
        recordQuietly(jwt, request, action, targetType, targetId, description, beforeValue, null, AuditMode.DELETE);
    }

    public void recordStatusChange(
            Jwt jwt,
            HttpServletRequest request,
            String action,
            String targetType,
            Object targetId,
            String description,
            Object beforeValue,
            Object afterValue
    ) {
        recordQuietly(jwt, request, action, targetType, targetId, description, beforeValue, afterValue, AuditMode.STATUS_CHANGE);
    }

    private void recordQuietly(
            Jwt jwt,
            HttpServletRequest request,
            String action,
            String targetType,
            Object targetId,
            String description,
            Object beforeValue,
            Object afterValue,
            AuditMode mode
    ) {
        try {
            AuditLogCommand command = new AuditLogCommand(
                    parseActorId(jwt),
                    jwt == null ? null : jwt.getClaimAsString("username"),
                    resolveActorRole(jwt),
                    action,
                    targetType,
                    targetId == null ? null : targetId.toString(),
                    description,
                    beforeValue,
                    afterValue,
                    resolveIpAddress(request),
                    request == null ? null : request.getHeader("User-Agent"),
                    null
            );

            switch (mode) {
                case CREATE -> auditLogService.recordCreate(command);
                case UPDATE -> auditLogService.recordUpdate(command);
                case DELETE -> auditLogService.recordDelete(command);
                case STATUS_CHANGE -> auditLogService.recordStatusChange(command);
            }
        } catch (RuntimeException exception) {
            log.warn("Failed to record audit log action={} targetType={} targetId={}", action, targetType, targetId, exception);
        }
    }

    private UUID parseActorId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String resolveActorRole(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        if (roles.contains("ADMIN")) {
            return "ADMIN";
        }
        if (roles.contains("STAFF")) {
            return "STAFF";
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .sorted(Comparator.naturalOrder())
                .findFirst()
                .orElse(null);
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private enum AuditMode {
        CREATE,
        UPDATE,
        DELETE,
        STATUS_CHANGE
    }
}
