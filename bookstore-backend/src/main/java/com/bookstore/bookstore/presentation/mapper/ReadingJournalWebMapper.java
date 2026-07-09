package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CheckInReadingStreakCommand;
import com.bookstore.bookstore.application.command.CreateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.DeleteReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.UpdateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.application.result.ReadingStreakResult;
import com.bookstore.bookstore.presentation.request.CheckInReadingStreakRequest;
import com.bookstore.bookstore.presentation.request.CreateReadingJournalEntryRequest;
import com.bookstore.bookstore.presentation.request.UpdateReadingJournalEntryRequest;
import com.bookstore.bookstore.presentation.response.ReadingJournalEntryResponse;
import com.bookstore.bookstore.presentation.response.ReadingStreakResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReadingJournalWebMapper {

    private final BookWebMapper bookWebMapper;

    public ReadingJournalWebMapper(BookWebMapper bookWebMapper) {
        this.bookWebMapper = bookWebMapper;
    }

    public CreateReadingJournalEntryCommand toCreateCommand(
            UUID userId,
            CreateReadingJournalEntryRequest request
    ) {
        return new CreateReadingJournalEntryCommand(
                userId,
                request.bookId(),
                request.entryDate(),
                request.note(),
                request.currentPage(),
                request.progressPercent()
        );
    }

    public UpdateReadingJournalEntryCommand toUpdateCommand(
            UUID entryId,
            UUID userId,
            UpdateReadingJournalEntryRequest request
    ) {
        return new UpdateReadingJournalEntryCommand(
                entryId,
                userId,
                request.note(),
                request.currentPage(),
                request.progressPercent()
        );
    }

    public DeleteReadingJournalEntryCommand toDeleteCommand(UUID entryId, UUID userId) {
        return new DeleteReadingJournalEntryCommand(entryId, userId);
    }

    public CheckInReadingStreakCommand toCheckInCommand(UUID userId, CheckInReadingStreakRequest request) {
        return new CheckInReadingStreakCommand(
                userId,
                request.bookId(),
                request.note(),
                request.currentPage(),
                request.progressPercent()
        );
    }

    public ReadingJournalEntryResponse toResponse(ReadingJournalEntryResult result) {
        return new ReadingJournalEntryResponse(
                result.id(),
                result.entryDate(),
                result.note(),
                result.currentPage(),
                result.progressPercent(),
                result.createdAt(),
                result.updatedAt(),
                bookWebMapper.toBookResponse(result.book())
        );
    }

    public ReadingStreakResponse toStreakResponse(ReadingStreakResult result) {
        return new ReadingStreakResponse(
                result.currentStreak(),
                result.longestStreak(),
                result.checkedInToday(),
                result.lastActivityDate()
        );
    }
}
