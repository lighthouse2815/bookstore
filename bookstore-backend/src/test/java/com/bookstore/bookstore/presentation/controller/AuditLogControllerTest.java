package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.AuditLogResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditLogController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuditLogService auditLogService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getAll_whenStaffAuthenticated_returnsFilteredPageWithHeaders() throws Exception {
        UUID logId = UUID.fromString("00000000-0000-0000-0000-000000000111");
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000222");
        given(auditLogService.getAll(
                new PageQuery(0, 10),
                "BOOK_UPDATED",
                "BOOK",
                actorId,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-07-08T23:59:59.999999999Z")
        )).willReturn(new PageSliceResult<>(
                List.of(new AuditLogResult(
                        logId,
                        actorId,
                        "admin",
                        "ADMIN",
                        "BOOK_UPDATED",
                        "BOOK",
                        "book-1",
                        "Cập nhật sách demo",
                        "{\"title\":\"old\"}",
                        "{\"title\":\"new\"}",
                        "127.0.0.1",
                        "JUnit",
                        Instant.parse("2026-07-08T12:00:00Z")
                )),
                1,
                0,
                10
        ));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                        .param("page", "0")
                        .param("size", "10")
                        .param("action", "BOOK_UPDATED")
                        .param("targetType", "BOOK")
                        .param("actorId", actorId.toString())
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-08"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(logId.toString()))
                .andExpect(jsonPath("$.data[0].action").value("BOOK_UPDATED"));
    }
}
