package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ReadingJournalAssembler;
import com.bookstore.bookstore.application.command.CreateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.DeleteReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.UpdateReadingJournalEntryCommand;
import com.bookstore.bookstore.application.command.UpsertReadingJournalEntryCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IReadingJournalService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingJournalService implements IReadingJournalService {

    private final IReadingJournalRepository readingJournalRepository;
    private final IBookRepository bookRepository;
    private final ReadingJournalAssembler readingJournalAssembler;

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReadingJournalEntryResult> getMyEntries(
            UUID userId,
            UUID bookId,
            LocalDate from,
            LocalDate to,
            PageQuery pageQuery
    ) {
        validateId(userId, "userId");
        validateOptionalBookId(bookId);
        validateDateRange(from, to);

        return readingJournalAssembler.toPageResult(
                readingJournalRepository.findPageByUserId(
                        userId,
                        bookId,
                        from,
                        to,
                        pageQuery.page(),
                        pageQuery.size()
                )
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingJournalEntryResult create(CreateReadingJournalEntryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = requireId(command.userId(), "userId");
        UUID bookId = requireId(command.bookId(), "bookId");
        LocalDate entryDate = requireDate(command.entryDate(), "entryDate");
        validateBookExists(bookId);
        ReadingJournalEntry existing = readingJournalRepository.findByUserIdAndBookIdAndEntryDate(
                userId,
                bookId,
                entryDate
        ).orElse(null);
        if (existing != null) {
            if (existing.isDeleted()) {
                existing.restore(
                        normalizeNote(command.note()),
                        command.currentPage(),
                        normalizeProgress(command.progressPercent())
                );
                return readingJournalAssembler.toResult(readingJournalRepository.save(existing));
            }
            throw new ApplicationException(ApplicationErrorCode.READING_JOURNAL_ENTRY_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        ReadingJournalEntry entry = new ReadingJournalEntry(
                UUID.randomUUID(),
                userId,
                bookId,
                entryDate,
                normalizeNote(command.note()),
                command.currentPage(),
                normalizeProgress(command.progressPercent()),
                now,
                now,
                null
        );
        return readingJournalAssembler.toResult(readingJournalRepository.save(entry));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingJournalEntryResult update(UpdateReadingJournalEntryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        ReadingJournalEntry entry = getOwnedEntryOrThrow(command.entryId(), command.userId());
        entry.update(
                normalizeNote(command.note()),
                command.currentPage(),
                normalizeProgress(command.progressPercent())
        );
        return readingJournalAssembler.toResult(readingJournalRepository.save(entry));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteReadingJournalEntryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        ReadingJournalEntry entry = getOwnedEntryOrThrow(command.entryId(), command.userId());
        entry.softDelete();
        readingJournalRepository.save(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingJournalEntryResult upsert(UpsertReadingJournalEntryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        validateBookExists(command.bookId());
        UUID userId = requireId(command.userId(), "userId");
        UUID bookId = requireId(command.bookId(), "bookId");
        LocalDate entryDate = requireDate(command.entryDate(), "entryDate");
        String note = normalizeNote(command.note());
        BigDecimal progressPercent = normalizeProgress(command.progressPercent());

        ReadingJournalEntry existing = readingJournalRepository.findByUserIdAndBookIdAndEntryDate(
                userId,
                bookId,
                entryDate
        ).orElse(null);
        if (existing == null) {
            Instant now = Instant.now();
            ReadingJournalEntry entry = new ReadingJournalEntry(
                    UUID.randomUUID(),
                    userId,
                    bookId,
                    entryDate,
                    note,
                    command.currentPage(),
                    progressPercent,
                    now,
                    now,
                    null
            );
            return readingJournalAssembler.toResult(readingJournalRepository.save(entry));
        }

        if (existing.isDeleted()) {
            existing.restore(note, command.currentPage(), progressPercent);
        } else {
            existing.update(note, command.currentPage(), progressPercent);
        }
        return readingJournalAssembler.toResult(readingJournalRepository.save(existing));
    }

    private ReadingJournalEntry getOwnedEntryOrThrow(UUID entryId, UUID userId) {
        validateId(entryId, "entryId");
        validateId(userId, "userId");
        return readingJournalRepository.findByIdAndUserIdActive(entryId, userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.READING_JOURNAL_ENTRY_NOT_FOUND));
    }

    private void validateBookExists(UUID bookId) {
        validateId(bookId, "bookId");
        bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
    }

    private void validateOptionalBookId(UUID bookId) {
        if (bookId != null) {
            validateId(bookId, "bookId");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "from");
        }
    }

    private UUID requireId(UUID value, String fieldName) {
        validateId(value, fieldName);
        return value;
    }

    private void validateId(UUID value, String fieldName) {
        if (value == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, fieldName);
        }
    }

    private LocalDate requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, fieldName);
        }
        return value;
    }

    private String normalizeNote(String note) {
        return StringUtils.trimToNull(note);
    }

    private BigDecimal normalizeProgress(BigDecimal progressPercent) {
        return progressPercent == null ? null : progressPercent.stripTrailingZeros();
    }
}
