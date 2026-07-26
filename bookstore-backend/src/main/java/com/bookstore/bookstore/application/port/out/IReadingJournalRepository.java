package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReadingJournalRepository {

    PageSliceResult<ReadingJournalEntry> findPageByUserId(
            UUID userId,
            UUID bookId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );

    Optional<ReadingJournalEntry> findByIdAndUserIdActive(UUID entryId, UUID userId);

    Optional<ReadingJournalEntry> findByUserIdAndBookIdAndEntryDate(UUID userId, UUID bookId, LocalDate entryDate);

    List<LocalDate> findDistinctEntryDatesByUserIdActive(UUID userId);

    List<ReadingJournalEntry> findAllByUserIdActive(UUID userId);

    ReadingJournalEntry save(ReadingJournalEntry entry);
}
