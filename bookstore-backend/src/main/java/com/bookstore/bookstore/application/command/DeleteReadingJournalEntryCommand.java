package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record DeleteReadingJournalEntryCommand(
        UUID entryId,
        UUID userId
) {
}
