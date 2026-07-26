package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReadingJournalEntry {

    private static final ZoneId ENTRY_ZONE = ZoneId.systemDefault();

    private UUID id;
    private UUID userId;
    private UUID bookId;
    private LocalDate entryDate;
    private String note;
    private Integer currentPage;
    private BigDecimal progressPercent;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public ReadingJournalEntry(
            UUID id,
            UUID userId,
            UUID bookId,
            LocalDate entryDate,
            String note,
            Integer currentPage,
            BigDecimal progressPercent,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_ID, "id");
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_USER_ID, "userId");
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_BOOK_ID, "bookId");
        setEntryDate(entryDate);
        setNote(note);
        setCurrentPage(currentPage);
        setProgressPercent(progressPercent);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void update(String note, Integer currentPage, BigDecimal progressPercent) {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.READING_JOURNAL_ENTRY_ALREADY_DELETED);
        }

        setNote(note);
        setCurrentPage(currentPage);
        setProgressPercent(progressPercent);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.READING_JOURNAL_ENTRY_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void restore(String note, Integer currentPage, BigDecimal progressPercent) {
        if (!isDeleted()) {
            throw new DomainException(DomainErrorCode.READING_JOURNAL_ENTRY_ALREADY_ACTIVE);
        }

        Instant now = Instant.now();
        setDeletedAt(null);
        setNote(note);
        setCurrentPage(currentPage);
        setProgressPercent(progressPercent);
        setUpdatedAt(now);
    }

    private void setEntryDate(LocalDate entryDate) {
        LocalDate normalized = Guard.notNull(
                entryDate,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DATE,
                "entryDate"
        );
        if (normalized.isAfter(LocalDate.now(ENTRY_ZONE))) {
            throw new DomainException(DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DATE, "entryDate");
        }
        this.entryDate = normalized;
    }

    private void setNote(String note) {
        this.note = Guard.notBlankOrNull(note, DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_NOTE, "note");
    }

    private void setCurrentPage(Integer currentPage) {
        if (currentPage != null && currentPage < 0) {
            throw new DomainException(DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CURRENT_PAGE, "currentPage");
        }
        this.currentPage = currentPage;
    }

    private void setProgressPercent(BigDecimal progressPercent) {
        if (progressPercent != null
                && (progressPercent.compareTo(BigDecimal.ZERO) < 0
                || progressPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new DomainException(
                    DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_PROGRESS_PERCENT,
                    "progressPercent"
            );
        }
        this.progressPercent = progressPercent;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CREATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_UPDATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DELETED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CREATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_UPDATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DELETED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CREATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_UPDATED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DELETED_AT,
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
