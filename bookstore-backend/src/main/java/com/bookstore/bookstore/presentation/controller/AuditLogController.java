package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.AuditLogResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AuditLogController {

    private final IAuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Instant fromInstant = from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to == null ? null : to.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
        var result = auditLogService.getAll(
                        new PageQuery(page, size),
                        action,
                        targetType,
                        actorId,
                        fromInstant,
                        toInstant
                )
                .map(this::toResponse);
        return ResponseEntity.ok()
                .headers(PaginationHeaderUtils.build(result))
                .body(ApiResponse.success(result.items()));
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditLogResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(toResponse(auditLogService.getById(id)));
    }

    private AuditLogResponse toResponse(AuditLogResult result) {
        return new AuditLogResponse(
                result.id(),
                result.actorId(),
                result.actorUsername(),
                result.actorRole(),
                result.action(),
                result.targetType(),
                result.targetId(),
                result.description(),
                result.beforeValue(),
                result.afterValue(),
                result.ipAddress(),
                result.userAgent(),
                result.createdAt()
        );
    }
}
