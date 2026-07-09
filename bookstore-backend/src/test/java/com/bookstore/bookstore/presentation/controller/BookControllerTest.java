package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IBookQueryService;
import com.bookstore.bookstore.application.port.in.IBookService;
import com.bookstore.bookstore.application.result.BookPageDetailResult;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.response.AuthorResponse;
import com.bookstore.bookstore.presentation.response.BookPageDetailResponse;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class BookControllerTest {

    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBookService bookService;

    @MockitoBean
    private IBookQueryService bookQueryService;

    @MockitoBean
    private BookWebMapper bookWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void getPageDetail_whenBookIdIsValid_returnsPageDetail() throws Exception {
        BookPageDetailResult pageDetailResult = org.mockito.Mockito.mock(BookPageDetailResult.class);
        given(bookQueryService.getPageDetail(BOOK_ID, 2)).willReturn(pageDetailResult);
        given(bookWebMapper.toBookPageDetailResponse(pageDetailResult)).willReturn(buildPageDetailResponse());

        mockMvc.perform(get("/api/books/{id}/page-detail", BOOK_ID)
                        .param("relatedLimit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.book.id").value(BOOK_ID.toString()))
                .andExpect(jsonPath("$.data.book.title").value("Sach demo"))
                .andExpect(jsonPath("$.data.author.name").value("Tac gia demo"))
                .andExpect(jsonPath("$.data.categoryTrail[0].id").value("00000000-0000-0000-0000-000000000201"));
    }

    @Test
    void getPageDetail_whenBookDoesNotExist_returnsNotFound() throws Exception {
        given(bookQueryService.getPageDetail(BOOK_ID, 8))
                .willThrow(new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        mockMvc.perform(get("/api/books/{id}/page-detail", BOOK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getPageDetail_whenBookIdIsInvalidUuid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/books/{id}/page-detail", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookQueryService, bookWebMapper);
    }

    private BookPageDetailResponse buildPageDetailResponse() {
        return new BookPageDetailResponse(
                new BookPageDetailResponse.DetailBookResponse(
                        BOOK_ID,
                        "Sach demo",
                        "9786040000000",
                        BigDecimal.valueOf(120_000),
                        null,
                        null,
                        15,
                        7,
                        "Mo ta demo",
                        List.of(),
                        null,
                        BigDecimal.valueOf(4.5),
                        2
                ),
                new AuthorResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000301"),
                        "Tac gia demo",
                        null,
                        null,
                        null,
                        null,
                        null,
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Instant.parse("2024-01-01T00:00:00Z")
                ),
                new BookPageDetailResponse.PublisherSummaryResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000401"),
                        "NXB demo"
                ),
                List.of(new BookPageDetailResponse.CategoryTrailItemResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000201"),
                        "Van hoc"
                )),
                new BookPageDetailResponse.RatingSummaryResponse(
                        BigDecimal.valueOf(4.5),
                        2,
                        Map.of(5, 1L, 4, 1L)
                ),
                List.of(),
                List.of()
        );
    }
}
