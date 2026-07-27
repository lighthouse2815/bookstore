package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.AuditLogAssembler;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.port.out.IAuditLogRepository;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.model.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    private static final String REDACTED_VALUE = "***REDACTED***";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IAuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(
                auditLogRepository,
                new AuditLogAssembler(),
                new ObjectMapper()
        );
    }

    @Test
    void recordCreate_sanitizesSensitiveFieldsBeforeSaving() throws Exception {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = auditLogService.recordCreate(new AuditLogCommand(
                UUID.randomUUID(),
                "admin",
                "ADMIN",
                "USER_CREATED",
                AuditTargetType.USER,
                UUID.randomUUID().toString(),
                "Tạo tài khoản",
                Map.of("password", "secret123", "email", "before@example.com"),
                Map.of("refreshToken", "abc", "username", "staff"),
                "127.0.0.1",
                "JUnit",
                Instant.parse("2026-07-08T12:00:00Z")
        ));

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertNotNull(result.id());
        assertEquals("USER_CREATED", savedLog.getAction());
        assertEquals(
                "***REDACTED***",
                objectMapper.readTree(savedLog.getBeforeValue()).get("password").asText()
        );
        assertEquals(
                "before@example.com",
                objectMapper.readTree(savedLog.getBeforeValue()).get("email").asText()
        );
        assertEquals(
                "***REDACTED***",
                objectMapper.readTree(savedLog.getAfterValue()).get("refreshToken").asText()
        );
        assertEquals(
                "staff",
                objectMapper.readTree(savedLog.getAfterValue()).get("username").asText()
        );
    }

    @Test
    void recordCreate_sanitizesSystemSensitiveFieldsInNestedPayloads() throws Exception {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<String> sensitiveFieldNames = List.of(
                "password",
                "newPassword",
                "passwordHash",
                "token",
                "accessToken",
                "refreshToken",
                "resetToken",
                "idToken",
                "tokenHash",
                "unsubscribeToken",
                "apiToken",
                "otpCode",
                "otpHash",
                "secret",
                "secretKey",
                "authorizationHeader",
                "apiKey",
                "webhookApiKey",
                "accessKey",
                "secretKeyHeader"
        );
        Map<String, Object> nestedPayload = new LinkedHashMap<>();
        sensitiveFieldNames.forEach(fieldName -> nestedPayload.put(fieldName, "sensitive-value"));
        nestedPayload.put("status", "ACTIVE");
        nestedPayload.put("tokenCount", 3);

        auditLogService.recordCreate(new AuditLogCommand(
                UUID.randomUUID(),
                "admin",
                "ADMIN",
                "INTEGRATION_UPDATED",
                AuditTargetType.USER,
                UUID.randomUUID().toString(),
                "Cập nhật cấu hình tích hợp",
                Map.of(
                        "security", List.of(nestedPayload),
                        "displayName", "Bookstore"
                ),
                null,
                "127.0.0.1",
                "JUnit",
                Instant.parse("2026-07-08T12:00:00Z")
        ));

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();
        var beforeValue = objectMapper.readTree(savedLog.getBeforeValue());
        var nestedValue = beforeValue.get("security").get(0);

        sensitiveFieldNames.forEach(fieldName ->
                assertEquals(REDACTED_VALUE, nestedValue.get(fieldName).asText(), fieldName)
        );
        assertEquals("ACTIVE", nestedValue.get("status").asText());
        assertEquals(3, nestedValue.get("tokenCount").asInt());
        assertEquals("Bookstore", beforeValue.get("displayName").asText());
    }

}
