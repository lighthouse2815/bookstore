package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IReviewService;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.ReviewWebMapper;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
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

@WebMvcTest(ReviewController.class)
@Import({SecurityConfig.class, ReviewWebMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class ReviewControllerTest {

    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000001111");
    private static final UUID REVIEW_ID = UUID.fromString("00000000-0000-0000-0000-000000001112");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000001113");
    private static final UUID ORDER_ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000001114");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000001115");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReviewService reviewService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void getByBookId_returnsApprovedReviewsOnlyForPublicEndpoint() throws Exception {
        given(reviewService.getByBookId(BOOK_ID)).willReturn(
                List.of(reviewResult(ReviewStatus.APPROVED, null, null, null))
        );

        mockMvc.perform(get("/api/books/{bookId}/reviews", BOOK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reviewId").value(REVIEW_ID.toString()))
                .andExpect(jsonPath("$.data[0].reviewerName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data[0].userId").doesNotExist())
                .andExpect(jsonPath("$.data[0].bookId").doesNotExist())
                .andExpect(jsonPath("$.data[0].orderItemId").doesNotExist())
                .andExpect(jsonPath("$.data[0].status").doesNotExist())
                .andExpect(jsonPath("$.data[0].moderationReason").doesNotExist())
                .andExpect(jsonPath("$.data[0].moderatedBy").doesNotExist());
    }

    @Test
    void getAll_whenAnonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_whenCustomerReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/reviews")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject(USER_ID.toString()).claim("roles", List.of("USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_whenAdminFiltersReviews_returnsPagedResponse() throws Exception {
        given(reviewService.getAll(0, 10, ReviewStatus.HIDDEN, BOOK_ID, USER_ID, 4)).willReturn(
                new PageSliceResult<>(
                        List.of(reviewResult(ReviewStatus.HIDDEN, "Spam", ADMIN_ID, Instant.parse("2026-07-08T10:00:00Z"))),
                        1,
                        0,
                        10
                )
        );

        mockMvc.perform(get("/api/admin/reviews")
                        .with(adminJwt())
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "HIDDEN")
                        .param("bookId", BOOK_ID.toString())
                        .param("userId", USER_ID.toString())
                        .param("rating", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reviewId").value(REVIEW_ID.toString()))
                .andExpect(jsonPath("$.data[0].status").value("HIDDEN"));
    }

    @Test
    void hide_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(reviewService.hide(any())).willReturn(
                reviewResult(ReviewStatus.HIDDEN, "Spam", ADMIN_ID, Instant.parse("2026-07-08T10:00:00Z"))
        );

        mockMvc.perform(put("/api/admin/reviews/{id}/hide", REVIEW_ID)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Spam"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.moderationReason").value("Spam"));

        verify(reviewService).hide(argThat(command ->
                command.reviewId().equals(REVIEW_ID)
                        && command.adminUserId().equals(ADMIN_ID)
                        && "Spam".equals(command.reason())
        ));
        verify(adminAuditSupport).recordStatusChange(
                any(),
                any(),
                eq("REVIEW_HIDDEN"),
                eq("REVIEW"),
                eq(REVIEW_ID),
                any(),
                any(),
                any()
        );
    }

    @Test
    void approve_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(reviewService.approve(any())).willReturn(
                reviewResult(ReviewStatus.APPROVED, null, ADMIN_ID, Instant.parse("2026-07-08T11:00:00Z"))
        );

        mockMvc.perform(put("/api/admin/reviews/{id}/approve", REVIEW_ID)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(reviewService).approve(argThat(command ->
                command.reviewId().equals(REVIEW_ID)
                        && command.adminUserId().equals(ADMIN_ID)
        ));
        verify(adminAuditSupport).recordStatusChange(
                any(),
                any(),
                eq("REVIEW_APPROVED"),
                eq("REVIEW"),
                eq(REVIEW_ID),
                any(),
                any(),
                any()
        );
    }

    @Test
    void adminDelete_whenAdminAuthenticated_recordsAuditLog() throws Exception {
        given(reviewService.adminDelete(REVIEW_ID)).willReturn(
                reviewResult(ReviewStatus.APPROVED, null, ADMIN_ID, Instant.parse("2026-07-08T11:30:00Z"))
        );

        mockMvc.perform(delete("/api/admin/reviews/{id}", REVIEW_ID)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminAuditSupport).recordDelete(
                any(),
                any(),
                eq("REVIEW_DELETED"),
                eq("REVIEW"),
                eq(REVIEW_ID),
                any(),
                any()
        );
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
        return jwt()
                .jwt(jwt -> jwt.subject(ADMIN_ID.toString()).claim("roles", List.of("ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private ReviewResult reviewResult(
            ReviewStatus status,
            String moderationReason,
            UUID moderatedBy,
            Instant moderatedAt
    ) {
        return new ReviewResult(
                REVIEW_ID,
                USER_ID,
                BOOK_ID,
                ORDER_ITEM_ID,
                "Nguyen Van A",
                null,
                true,
                List.of(),
                0L,
                4,
                "Noi dung review",
                status,
                moderationReason,
                moderatedBy,
                "Admin Demo",
                moderatedAt,
                Instant.parse("2026-07-08T09:00:00Z"),
                Instant.parse("2026-07-08T09:05:00Z")
        );
    }
}
