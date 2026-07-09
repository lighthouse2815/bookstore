package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.command.CheckInReadingStreakCommand;
import com.bookstore.bookstore.application.port.in.IReadingStreakService;
import com.bookstore.bookstore.application.result.ReadingStreakResult;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.ReadingJournalWebMapper;
import com.bookstore.bookstore.presentation.request.CheckInReadingStreakRequest;
import com.bookstore.bookstore.presentation.response.ReadingStreakResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReadingStreakController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class ReadingStreakControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReadingStreakService readingStreakService;

    @MockitoBean
    private ReadingJournalWebMapper readingJournalWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getMyStreak_returnsSummary() throws Exception {
        ReadingStreakResult result = new ReadingStreakResult(3, 7, false, LocalDate.of(2026, 7, 9));
        ReadingStreakResponse response = new ReadingStreakResponse(3, 7, false, LocalDate.of(2026, 7, 9));

        given(readingStreakService.getMyStreak(USER_ID)).willReturn(result);
        given(readingJournalWebMapper.toStreakResponse(result)).willReturn(response);

        mockMvc.perform(get("/api/reading-streak")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.longestStreak").value(7));
    }

    @Test
    void checkIn_returnsUpdatedSummary() throws Exception {
        CheckInReadingStreakRequest request = new CheckInReadingStreakRequest(BOOK_ID, "Doc tiep", 12, null);
        CheckInReadingStreakCommand command = new CheckInReadingStreakCommand(USER_ID, BOOK_ID, "Doc tiep", 12, null);
        ReadingStreakResult result = new ReadingStreakResult(4, 7, true, LocalDate.of(2026, 7, 10));
        ReadingStreakResponse response = new ReadingStreakResponse(4, 7, true, LocalDate.of(2026, 7, 10));

        given(readingJournalWebMapper.toCheckInCommand(USER_ID, request)).willReturn(command);
        given(readingStreakService.checkIn(command)).willReturn(result);
        given(readingJournalWebMapper.toStreakResponse(result)).willReturn(response);

        mockMvc.perform(post("/api/reading-streak/check-in")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "%s",
                                  "note": "Doc tiep",
                                  "currentPage": 12
                                }
                                """.formatted(BOOK_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStreak").value(4))
                .andExpect(jsonPath("$.data.checkedInToday").value(true));
    }
}
