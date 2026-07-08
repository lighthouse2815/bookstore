package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IWishlistService;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
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

@WebMvcTest(WishlistController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class WishlistControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IWishlistService wishlistService;

    @MockitoBean
    private BookWebMapper bookWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getMyWishlist_whenAuthenticated_returnsBookCards() throws Exception {
        BookQueryResult result = new BookQueryResult(
                book(BOOK_ID),
                12L,
                new BookRatingSummaryResult(new BigDecimal("4.8"), 5L, Map.of(5, 5L))
        );
        BookResponse response = new BookResponse(
                BOOK_ID,
                "Sach yeu thich",
                "ISBN-001",
                "Mo ta",
                new BigDecimal("120000"),
                8,
                12L,
                new BigDecimal("4.8"),
                5L,
                Map.of(5, 5L),
                "/covers/book.jpg",
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.EPOCH,
                Instant.EPOCH
        );

        given(wishlistService.getMyWishlist(USER_ID)).willReturn(List.of(result));
        given(bookWebMapper.toBookResponse(result)).willReturn(response);

        mockMvc.perform(get("/api/wishlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(BOOK_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value("Sach yeu thich"));
    }

    @Test
    void addBook_whenAuthenticated_returnsSuccessMessage() throws Exception {
        willDoNothing().given(wishlistService).addBook(USER_ID, BOOK_ID);

        mockMvc.perform(post("/api/wishlist/items/{bookId}", BOOK_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Added"));
    }

    @Test
    void removeBook_whenAuthenticated_returnsSuccessMessage() throws Exception {
        willDoNothing().given(wishlistService).removeBook(USER_ID, BOOK_ID);

        mockMvc.perform(delete("/api/wishlist/items/{bookId}", BOOK_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deleted"));
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Sach yeu thich",
                "ISBN-001",
                "Mo ta",
                new BigDecimal("120000"),
                8,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
