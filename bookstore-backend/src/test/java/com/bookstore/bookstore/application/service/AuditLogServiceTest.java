package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.AuditLogAssembler;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.port.out.IAuditLogRepository;
import com.bookstore.bookstore.domain.model.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
                "USER",
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
}
