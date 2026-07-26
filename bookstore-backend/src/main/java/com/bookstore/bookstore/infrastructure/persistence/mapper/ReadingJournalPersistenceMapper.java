package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingJournalEntryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReadingJournalPersistenceMapper {

    public ReadingJournalEntry toDomain(ReadingJournalEntryJpaEntity entity) {
        return new ReadingJournalEntry(
                entity.getId(),
                entity.getUser().getId(),
                entity.getBook().getId(),
                entity.getEntryDate(),
                entity.getNote(),
                entity.getCurrentPage(),
                entity.getProgressPercent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            ReadingJournalEntry entry,
            ReadingJournalEntryJpaEntity entity,
            UserJpaEntity user,
            BookJpaEntity book
    ) {
        entity.setId(entry.getId());
        entity.setUser(user);
        entity.setBook(book);
        entity.setEntryDate(entry.getEntryDate());
        entity.setNote(entry.getNote());
        entity.setCurrentPage(entry.getCurrentPage());
        entity.setProgressPercent(entry.getProgressPercent());
        entity.setCreatedAt(entry.getCreatedAt());
        entity.setUpdatedAt(entry.getUpdatedAt());
        entity.setDeletedAt(entry.getDeletedAt());
    }
}
