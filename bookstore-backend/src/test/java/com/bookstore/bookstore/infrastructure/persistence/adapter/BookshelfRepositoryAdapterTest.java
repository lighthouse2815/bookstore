package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookshelfItemPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookshelfPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookshelfItemJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookshelfJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookshelfRepositoryAdapterTest {

    @Mock
    private BookshelfJpaRepository bookshelfJpaRepository;

    @Mock
    private BookshelfItemJpaRepository bookshelfItemJpaRepository;

    @Mock
    private BookshelfPersistenceMapper bookshelfPersistenceMapper;

    @Mock
    private BookshelfItemPersistenceMapper bookshelfItemPersistenceMapper;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @InjectMocks
    private BookshelfRepositoryAdapter bookshelfRepositoryAdapter;

    @Test
    void countActiveItemsByShelfIds_mapsGroupedRowsIntoCountMap() {
        UUID firstShelfId = UUID.randomUUID();
        UUID secondShelfId = UUID.randomUUID();

        when(bookshelfItemJpaRepository.countActiveByShelfIds(List.of(firstShelfId, secondShelfId))).thenReturn(
                List.of(
                        new Object[]{firstShelfId, 2L},
                        new Object[]{secondShelfId, 5L}
                )
        );

        Map<UUID, Long> result = bookshelfRepositoryAdapter.countActiveItemsByShelfIds(
                List.of(firstShelfId, secondShelfId)
        );

        assertEquals(Map.of(firstShelfId, 2L, secondShelfId, 5L), result);
    }

    @Test
    void saveItem_reusesExistingEntityAndResolvesShelfAndBookReferences() {
        UUID shelfId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BookshelfItem item = bookshelfItem(shelfId, bookId);
        BookshelfItemJpaEntity entity = new BookshelfItemJpaEntity();
        BookshelfJpaEntity shelf = new BookshelfJpaEntity();
        shelf.setId(shelfId);
        BookJpaEntity book = new BookJpaEntity();
        book.setId(bookId);

        when(bookshelfItemJpaRepository.findById(item.getId())).thenReturn(Optional.of(entity));
        when(bookshelfJpaRepository.getReferenceById(shelfId)).thenReturn(shelf);
        when(bookJpaRepository.getReferenceById(bookId)).thenReturn(book);
        when(bookshelfItemJpaRepository.save(entity)).thenReturn(entity);
        when(bookshelfItemPersistenceMapper.toDomain(entity)).thenReturn(item);

        BookshelfItem result = bookshelfRepositoryAdapter.saveItem(item);

        assertSame(item, result);
        verify(bookshelfItemPersistenceMapper).copyToEntity(item, entity, shelf, book);
    }

    private static BookshelfItem bookshelfItem(UUID shelfId, UUID bookId) {
        Instant now = Instant.EPOCH;
        return new BookshelfItem(
                UUID.randomUUID(),
                shelfId,
                bookId,
                0,
                now,
                now,
                null
        );
    }
}
