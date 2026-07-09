package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CheckInReadingStreakCommand;
import com.bookstore.bookstore.application.command.UpsertReadingJournalEntryCommand;
import com.bookstore.bookstore.application.port.in.IReadingJournalService;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.application.result.ReadingStreakResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadingStreakServiceTest {

    @Mock
    private IReadingJournalRepository readingJournalRepository;

    @Mock
    private IReadingJournalService readingJournalService;

    @InjectMocks
    private ReadingStreakService readingStreakService;

    @Test
    void getMyStreak_whenLatestActivityHasGap_resetsCurrentButKeepsLongest() {
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = List.of(
                today.minusDays(6),
                today.minusDays(5),
                today.minusDays(4),
                today.minusDays(2)
        );

        when(readingJournalRepository.findDistinctEntryDatesByUserIdActive(userId)).thenReturn(dates);

        ReadingStreakResult result = readingStreakService.getMyStreak(userId);

        assertEquals(0, result.currentStreak());
        assertEquals(3, result.longestStreak());
        assertFalse(result.checkedInToday());
        assertEquals(today.minusDays(2), result.lastActivityDate());
    }

    @Test
    void getMyStreak_whenNoActivity_returnsEmptySummary() {
        UUID userId = UUID.randomUUID();
        when(readingJournalRepository.findDistinctEntryDatesByUserIdActive(userId)).thenReturn(List.of());

        ReadingStreakResult result = readingStreakService.getMyStreak(userId);

        assertEquals(0, result.currentStreak());
        assertEquals(0, result.longestStreak());
        assertFalse(result.checkedInToday());
        assertNull(result.lastActivityDate());
    }

    @Test
    void checkIn_upsertsTodayAndReturnsUpdatedSummary() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        when(readingJournalService.upsert(org.mockito.ArgumentMatchers.any(UpsertReadingJournalEntryCommand.class)))
                .thenReturn(new ReadingJournalEntryResult(
                        UUID.randomUUID(),
                        today,
                        "Doc tiep",
                        20,
                        null,
                        Instant.EPOCH,
                        Instant.EPOCH,
                        null
                ));
        when(readingJournalRepository.findDistinctEntryDatesByUserIdActive(userId))
                .thenReturn(List.of(today.minusDays(1), today));

        ReadingStreakResult result = readingStreakService.checkIn(new CheckInReadingStreakCommand(
                userId,
                bookId,
                "Doc tiep",
                20,
                null
        ));

        ArgumentCaptor<UpsertReadingJournalEntryCommand> commandCaptor =
                ArgumentCaptor.forClass(UpsertReadingJournalEntryCommand.class);
        verify(readingJournalService).upsert(commandCaptor.capture());
        assertEquals(today, commandCaptor.getValue().entryDate());
        assertEquals(bookId, commandCaptor.getValue().bookId());
        assertEquals(2, result.currentStreak());
        assertEquals(2, result.longestStreak());
    }
}
