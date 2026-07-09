package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.DeleteReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.UpdateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.UpsertReadingJournalEntryCommand;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import java.time.LocalDate;
import java.util.UUID;

public interface IReadingJournalService {

    PageSliceResult<ReadingJournalEntryResult> getMyEntries(
            UUID userId,
            UUID bookId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );

    ReadingJournalEntryResult create(CreateReadingJournalEntryCommand command);

    ReadingJournalEntryResult update(UpdateReadingJournalEntryCommand command);

    void delete(DeleteReadingJournalEntryCommand command);

    ReadingJournalEntryResult upsert(UpsertReadingJournalEntryCommand command);
}
