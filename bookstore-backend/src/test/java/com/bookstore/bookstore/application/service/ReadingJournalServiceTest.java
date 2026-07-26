package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ReadingJournalAssembler;
import com.bookstore.bookstore.application.command.CreateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadingJournalServiceTest {

    @Mock
    private IReadingJournalRepository readingJournalRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private ReadingJournalAssembler readingJournalAssembler;

    @InjectMocks
    private ReadingJournalService readingJournalService;

    @Test
    void create_whenDeletedEntryExists_restoresExistingEntry() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.of(2026, 7, 10);
        ReadingJournalEntry deletedEntry = entry(userId, bookId, entryDate, true);
        ReadingJournalEntryResult expected = result(deletedEntry);

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(readingJournalRepository.findByUserIdAndBookIdAndEntryDate(userId, bookId, entryDate))
                .thenReturn(Optional.of(deletedEntry));
        when(readingJournalRepository.save(deletedEntry)).thenReturn(deletedEntry);
        when(readingJournalAssembler.toResult(deletedEntry)).thenReturn(expected);

        ReadingJournalEntryResult result = readingJournalService.create(new CreateReadingJournalEntryCommand(
                userId,
                bookId,
                entryDate,
                "  Ghi chu  ",
                12,
                new BigDecimal("45")
        ));

        assertFalse(deletedEntry.isDeleted());
        assertEquals(expected, result);
        verify(readingJournalRepository).save(deletedEntry);
    }

    @Test
    void create_whenActiveDuplicateExists_throwsConflict() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        LocalDate entryDate = LocalDate.of(2026, 7, 10);
        ReadingJournalEntry activeEntry = entry(userId, bookId, entryDate, false);

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(readingJournalRepository.findByUserIdAndBookIdAndEntryDate(userId, bookId, entryDate))
                .thenReturn(Optional.of(activeEntry));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> readingJournalService.create(new CreateReadingJournalEntryCommand(
                        userId,
                        bookId,
                        entryDate,
                        "Trung",
                        null,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.READING_JOURNAL_ENTRY_ALREADY_EXISTS, exception.getErrorCode());
    }

    private static ReadingJournalEntry entry(UUID userId, UUID bookId, LocalDate entryDate, boolean deleted) {
        Instant now = Instant.EPOCH;
        return new ReadingJournalEntry(
                UUID.randomUUID(),
                userId,
                bookId,
                entryDate,
                "Ghi chu",
                10,
                new BigDecimal("20"),
                now,
                now,
                deleted ? now.plusSeconds(1) : null
        );
    }

    private static ReadingJournalEntryResult result(ReadingJournalEntry entry) {
        return new ReadingJournalEntryResult(
                entry.getId(),
                entry.getEntryDate(),
                entry.getNote(),
                entry.getCurrentPage(),
                entry.getProgressPercent(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                null
        );
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Sach",
                "ISBN-001",
                "Mo ta",
                new BigDecimal("120000"),
                9,
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
