package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingJournalEntryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ReadingJournalPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ReadingJournalEntryJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReadingJournalRepositoryAdapter implements IReadingJournalRepository {

    private final ReadingJournalEntryJpaRepository readingJournalEntryJpaRepository;
    private final ReadingJournalPersistenceMapper readingJournalPersistenceMapper;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public PageSliceResult<ReadingJournalEntry> findPageByUserId(
            UUID userId,
            UUID bookId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        var resultPage = readingJournalEntryJpaRepository.findPageByUserId(
                userId,
                bookId,
                from,
                to,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.stream()
                        .map(readingJournalPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Optional<ReadingJournalEntry> findByIdAndUserIdActive(UUID entryId, UUID userId) {
        return readingJournalEntryJpaRepository.findByIdAndUserIdActive(entryId, userId)
                .map(readingJournalPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ReadingJournalEntry> findByUserIdAndBookIdAndEntryDate(
            UUID userId,
            UUID bookId,
            LocalDate entryDate
    ) {
        return readingJournalEntryJpaRepository.findByUserIdAndBookIdAndEntryDate(userId, bookId, entryDate)
                .map(readingJournalPersistenceMapper::toDomain);
    }

    @Override
    public List<LocalDate> findDistinctEntryDatesByUserIdActive(UUID userId) {
        return readingJournalEntryJpaRepository.findDistinctEntryDatesByUserIdActive(userId);
    }

    @Override
    public List<ReadingJournalEntry> findAllByUserIdActive(UUID userId) {
        return readingJournalEntryJpaRepository.findAllByUserIdActive(userId).stream()
                .map(readingJournalPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public ReadingJournalEntry save(ReadingJournalEntry entry) {
        ReadingJournalEntryJpaEntity entity = readingJournalEntryJpaRepository.findById(entry.getId())
                .orElseGet(ReadingJournalEntryJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(entry.getUserId());
        BookJpaEntity book = bookJpaRepository.getReferenceById(entry.getBookId());
        readingJournalPersistenceMapper.copyToEntity(entry, entity, user, book);
        return readingJournalPersistenceMapper.toDomain(readingJournalEntryJpaRepository.save(entity));
    }
}
