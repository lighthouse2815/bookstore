package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.command.CreateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.port.in.IReadingJournalService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.ReadingJournalWebMapper;
import com.bookstore.bookstore.presentation.request.CreateReadingJournalEntryRequest;
import com.bookstore.bookstore.presentation.response.ReadingJournalEntryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(ReadingJournalController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class ReadingJournalControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID ENTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");
    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000333");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2026, 7, 10);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReadingJournalService readingJournalService;

    @MockitoBean
    private ReadingJournalWebMapper readingJournalWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getMyEntries_returnsPagedEntriesWithHeaders() throws Exception {
        ReadingJournalEntryResult result = new ReadingJournalEntryResult(
                ENTRY_ID,
                ENTRY_DATE,
                "Ghi chu",
                15,
                new BigDecimal("32.5"),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
        ReadingJournalEntryResponse response = new ReadingJournalEntryResponse(
                ENTRY_ID,
                ENTRY_DATE,
                "Ghi chu",
                15,
                new BigDecimal("32.5"),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        given(readingJournalService.getMyEntries(USER_ID, null, null, null, new PageQuery(0, 12)))
                .willReturn(new PageSliceResult<>(List.of(result), 1L, 0, 12));
        given(readingJournalWebMapper.toResponse(result)).willReturn(response);

        mockMvc.perform(get("/api/reading-journal")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(ENTRY_ID.toString()))
                .andExpect(jsonPath("$.data[0].entryDate").value("2026-07-10"));
    }

    @Test
    void create_returnsCreatedEntry() throws Exception {
        CreateReadingJournalEntryRequest request = new CreateReadingJournalEntryRequest(
                BOOK_ID,
                ENTRY_DATE,
                "Ghi chu",
                21,
                new BigDecimal("40")
        );
        CreateReadingJournalEntryCommand command = new CreateReadingJournalEntryCommand(
                USER_ID,
                BOOK_ID,
                ENTRY_DATE,
                "Ghi chu",
                21,
                new BigDecimal("40")
        );
        ReadingJournalEntryResult result = new ReadingJournalEntryResult(
                ENTRY_ID,
                ENTRY_DATE,
                "Ghi chu",
                21,
                new BigDecimal("40"),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
        ReadingJournalEntryResponse response = new ReadingJournalEntryResponse(
                ENTRY_ID,
                ENTRY_DATE,
                "Ghi chu",
                21,
                new BigDecimal("40"),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        given(readingJournalWebMapper.toCreateCommand(USER_ID, request)).willReturn(command);
        given(readingJournalService.create(command)).willReturn(result);
        given(readingJournalWebMapper.toResponse(result)).willReturn(response);

        mockMvc.perform(post("/api/reading-journal")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "%s",
                                  "entryDate": "2026-07-10",
                                  "note": "Ghi chu",
                                  "currentPage": 21,
                                  "progressPercent": 40
                                }
                                """.formatted(BOOK_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(ENTRY_ID.toString()))
                .andExpect(jsonPath("$.data.entryDate").value("2026-07-10"));
    }
}
