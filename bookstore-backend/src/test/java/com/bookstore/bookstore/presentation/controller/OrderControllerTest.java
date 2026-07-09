package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.OrderTimelineWebMapper;
import com.bookstore.bookstore.presentation.mapper.OrderWebMapper;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, OrderWebMapper.class, OrderTimelineWebMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class OrderControllerTest {

    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private IOrderTimelineService orderTimelineService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void updateStatus_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(orderService.getById(ORDER_ID)).willReturn(buildOrderResult(OrderStatus.PENDING));
        given(orderService.updateStatus(any())).willReturn(buildOrderResult(OrderStatus.CONFIRMED));

        mockMvc.perform(put("/api/admin/orders/{id}/status", ORDER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONFIRMED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        verify(adminAuditSupport).recordStatusChange(
                any(),
                any(),
                eq("ORDER_STATUS_UPDATED"),
                eq("ORDER"),
                eq(ORDER_ID),
                any(),
                any(),
                any()
        );
    }

    @Test
    void getMyOrderTimeline_whenUserAuthenticated_returnsTimeline() throws Exception {
        given(orderTimelineService.getMyTimeline(any(), eq(ORDER_ID))).willReturn(List.of(buildTimelineEvent()));

        mockMvc.perform(get("/api/orders/{id}/timeline", ORDER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000444"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("ORDER_CREATED"))
                .andExpect(jsonPath("$.data[0].title").value("Đơn hàng đã được tạo"));
    }

    @Test
    void getAdminOrderTimeline_whenStaffAuthenticated_returnsTimeline() throws Exception {
        given(orderTimelineService.getOrderTimeline(ORDER_ID)).willReturn(List.of(buildTimelineEvent()));

        mockMvc.perform(get("/api/admin/orders/{id}/timeline", ORDER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("ORDER_CREATED"));
    }

    private OrderResult buildOrderResult(OrderStatus status) {
        return new OrderResult(
                ORDER_ID,
                "DH-TEST-001",
                UUID.fromString("00000000-0000-0000-0000-000000000444"),
                List.of(),
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100000"),
                null,
                null,
                null,
                null,
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                status,
                "Nguyen Van A",
                "0900000000",
                "123 Test Street",
                Instant.parse("2026-07-08T12:00:00Z"),
                Instant.parse("2026-07-08T12:30:00Z"),
                null
        );
    }

    private OrderTimelineEventResult buildTimelineEvent() {
        return new OrderTimelineEventResult(
                UUID.fromString("00000000-0000-0000-0000-000000000999"),
                ORDER_ID,
                "ORDER_CREATED",
                "Đơn hàng đã được tạo",
                "Đơn hàng DH-TEST-001 đã được tạo thành công.",
                null,
                null,
                "admin",
                "ADMIN",
                Instant.parse("2026-07-08T12:00:00Z"),
                null
        );
    }
}
