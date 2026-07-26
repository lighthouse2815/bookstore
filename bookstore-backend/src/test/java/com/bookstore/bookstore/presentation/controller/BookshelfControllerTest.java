package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.command.CreateBookshelfCommand;
import com.bookstore.bookstore.application.command.DeleteBookshelfCommand;
import com.bookstore.bookstore.application.port.in.IBookshelfService;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.BookshelfWebMapper;
import com.bookstore.bookstore.presentation.request.CreateBookshelfRequest;
import com.bookstore.bookstore.presentation.response.BookshelfResponse;
import com.bookstore.bookstore.presentation.response.BookshelfSummaryResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookshelfController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class BookshelfControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID SHELF_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBookshelfService bookshelfService;

    @MockitoBean
    private BookshelfWebMapper bookshelfWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getMyBookshelves_whenAuthenticated_returnsSummaryList() throws Exception {
        BookshelfSummaryResult result = new BookshelfSummaryResult(
                SHELF_ID,
                "Doc sau",
                4L,
                Instant.EPOCH,
                Instant.EPOCH
        );
        BookshelfSummaryResponse response = new BookshelfSummaryResponse(
                SHELF_ID,
                "Doc sau",
                4L,
                Instant.EPOCH,
                Instant.EPOCH
        );

        given(bookshelfService.getMyBookshelves(USER_ID)).willReturn(List.of(result));
        given(bookshelfWebMapper.toSummaryResponse(result)).willReturn(response);

        mockMvc.perform(get("/api/bookshelves")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(SHELF_ID.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Doc sau"));
    }

    @Test
    void create_whenAuthenticated_returnsCreatedShelf() throws Exception {
        CreateBookshelfRequest request = new CreateBookshelfRequest("Qua tang");
        CreateBookshelfCommand command = new CreateBookshelfCommand(USER_ID, "Qua tang");
        BookshelfResult result = new BookshelfResult(
                SHELF_ID,
                "Qua tang",
                0L,
                List.of(),
                Instant.EPOCH,
                Instant.EPOCH
        );
        BookshelfResponse response = new BookshelfResponse(
                SHELF_ID,
                "Qua tang",
                0L,
                List.of(),
                Instant.EPOCH,
                Instant.EPOCH
        );

        given(bookshelfWebMapper.toCreateCommand(USER_ID, request)).willReturn(command);
        given(bookshelfService.create(command)).willReturn(result);
        given(bookshelfWebMapper.toResponse(result)).willReturn(response);

        mockMvc.perform(post("/api/bookshelves")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Qua tang"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(SHELF_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("Qua tang"));
    }

    @Test
    void delete_whenAuthenticated_returnsDeletedMessage() throws Exception {
        DeleteBookshelfCommand command = new DeleteBookshelfCommand(SHELF_ID, USER_ID);

        given(bookshelfWebMapper.toDeleteCommand(SHELF_ID, USER_ID)).willReturn(command);
        willDoNothing().given(bookshelfService).delete(command);

        mockMvc.perform(delete("/api/bookshelves/{shelfId}", SHELF_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deleted"));
    }
}
