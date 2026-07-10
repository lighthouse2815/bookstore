package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IPersonalizedRecommendationService;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.application.result.PersonalizedRecommendationResult;
import com.bookstore.bookstore.application.result.RecommendationReasonCode;
import com.bookstore.bookstore.application.result.RecommendationStrategy;
import com.bookstore.bookstore.application.result.RecommendedBookResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.response.BookResponse;
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

@WebMvcTest(BookRecommendationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class BookRecommendationControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPersonalizedRecommendationService personalizedRecommendationService;

    @MockitoBean
    private BookWebMapper bookWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getPersonalized_usesAuthenticatedSubjectAndReturnsOnlyBookCardDataAndReasons() throws Exception {
        Book book = book();
        BookQueryResult bookResult = new BookQueryResult(
                book,
                7L,
                new BookRatingSummaryResult(BigDecimal.valueOf(4.5), 2L, Map.of(5, 2L))
        );
        PersonalizedRecommendationResult result = new PersonalizedRecommendationResult(
                List.of(new RecommendedBookResult(
                        bookResult,
                        List.of(RecommendationReasonCode.FAVORITE_CATEGORY, RecommendationReasonCode.HIGH_RATING)
                )),
                RecommendationStrategy.PERSONALIZED,
                true,
                Instant.parse("2026-07-10T12:00:00Z")
        );
        BookResponse response = new BookResponse(
                BOOK_ID, "Book title", "ISBN-1", "Description", BigDecimal.TEN, 3, 7L,
                BigDecimal.valueOf(4.5), 2L, Map.of(5, 2L), null, List.of(), null,
                book.getCategoryId(), book.getAuthorId(), book.getPublisherId(), Instant.EPOCH, Instant.EPOCH
        );
        given(personalizedRecommendationService.getForUser(USER_ID, 12)).willReturn(result);
        given(bookWebMapper.toBookResponse(any(BookQueryResult.class))).willReturn(response);

        mockMvc.perform(get("/api/books/recommendations/personalized")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.strategy").value("PERSONALIZED"))
                .andExpect(jsonPath("$.data.hasPersonalSignals").value(true))
                .andExpect(jsonPath("$.data.items[0].book.id").value(BOOK_ID.toString()))
                .andExpect(jsonPath("$.data.items[0].reasonCodes[0]").value("FAVORITE_CATEGORY"))
                .andExpect(jsonPath("$..note").doesNotExist())
                .andExpect(jsonPath("$..userId").doesNotExist());

        verify(personalizedRecommendationService).getForUser(USER_ID, 12);
    }

    @Test
    void getPersonalized_rejectsAnonymousAndOutOfRangeLimits() throws Exception {
        mockMvc.perform(get("/api/books/recommendations/personalized"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/books/recommendations/personalized")
                        .param("limit", "25")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    private static Book book() {
        return new Book(
                BOOK_ID,
                "Book title",
                "ISBN-1",
                "Description",
                BigDecimal.TEN,
                3,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }
}
