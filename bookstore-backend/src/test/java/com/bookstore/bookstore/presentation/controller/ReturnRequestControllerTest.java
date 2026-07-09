package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IReturnRequestService;
import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.ReturnRequestWebMapper;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReturnRequestController.class)
@Import({SecurityConfig.class, ReturnRequestWebMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class ReturnRequestControllerTest {

    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReturnRequestService returnRequestService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void create_whenUserAuthenticated_returnsCreated() throws Exception {
        given(returnRequestService.create(any())).willReturn(buildResult(ReturnRequestStatus.PENDING));

        mockMvc.perform(post("/api/orders/{orderId}/return-request", ORDER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000333")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Sách bị rách bìa",
                                  "requestedRefundAmount": 50000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.orderCode").value("DH-RETURN-001"));
    }

    @Test
    void approve_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(returnRequestService.getById(REQUEST_ID)).willReturn(buildResult(ReturnRequestStatus.PENDING));
        given(returnRequestService.approve(any())).willReturn(buildResult(ReturnRequestStatus.APPROVED));

        mockMvc.perform(put("/api/admin/return-requests/{id}/approve", REQUEST_ID)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000444"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminNote": "Đồng ý hoàn tiền",
                                  "approvedRefundAmount": 40000,
                                  "restock": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(adminAuditSupport).recordStatusChange(
                any(),
                any(),
                eq("RETURN_APPROVED"),
                eq("RETURN_REQUEST"),
                eq(REQUEST_ID),
                any(),
                any(),
                any()
        );
    }

    @Test
    void reject_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(returnRequestService.getById(REQUEST_ID)).willReturn(buildResult(ReturnRequestStatus.PENDING));
        given(returnRequestService.reject(any())).willReturn(buildResult(ReturnRequestStatus.REJECTED));

        mockMvc.perform(put("/api/admin/return-requests/{id}/reject", REQUEST_ID)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000444"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminNote": "Không đủ điều kiện"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        verify(adminAuditSupport).recordStatusChange(
                any(),
                any(),
                eq("RETURN_REJECTED"),
                eq("RETURN_REQUEST"),
                eq(REQUEST_ID),
                any(),
                any(),
                any()
        );
    }

    private ReturnRequestResult buildResult(ReturnRequestStatus status) {
        return new ReturnRequestResult(
                REQUEST_ID,
                ORDER_ID,
                "DH-RETURN-001",
                UUID.fromString("00000000-0000-0000-0000-000000000333"),
                "customer",
                "customer@example.com",
                "Nguyen Van A",
                "Sách bị rách bìa",
                status,
                new BigDecimal("50000"),
                status == ReturnRequestStatus.APPROVED ? new BigDecimal("40000") : null,
                status == ReturnRequestStatus.REJECTED ? "Không đủ điều kiện" : "Đồng ý hoàn tiền",
                status == ReturnRequestStatus.PENDING ? null : UUID.fromString("00000000-0000-0000-0000-000000000444"),
                status == ReturnRequestStatus.PENDING ? null : "admin",
                status == ReturnRequestStatus.PENDING ? null : Instant.parse("2026-07-09T02:00:00Z"),
                OrderStatus.DELIVERED,
                PaymentMethod.COD,
                PaymentStatus.PAID,
                new BigDecimal("120000"),
                Instant.parse("2026-07-08T02:00:00Z"),
                Instant.parse("2026-07-09T01:00:00Z"),
                Instant.parse("2026-07-09T02:00:00Z")
        );
    }
}
