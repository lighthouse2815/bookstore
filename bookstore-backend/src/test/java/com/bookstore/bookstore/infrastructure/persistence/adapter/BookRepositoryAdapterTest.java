package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuthorJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CategoryJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.FileAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.PublisherJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class BookRepositoryAdapterTest {

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private BookPersistenceMapper bookPersistenceMapper;

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private AuthorJpaRepository authorJpaRepository;

    @Mock
    private PublisherJpaRepository publisherJpaRepository;

    @Mock
    private FileAssetJpaRepository fileAssetJpaRepository;

    @InjectMocks
    private BookRepositoryAdapter bookRepositoryAdapter;

    @Test
    void findPageActive_preservesIdPageOrderAfterGraphFetch() {
        UUID newestBookId = UUID.randomUUID();
        UUID olderBookId = UUID.randomUUID();
        BookJpaEntity newestBookEntity = bookEntity(newestBookId);
        BookJpaEntity olderBookEntity = bookEntity(olderBookId);
        Book newestBook = org.mockito.Mockito.mock(Book.class);
        Book olderBook = org.mockito.Mockito.mock(Book.class);

        when(bookJpaRepository.findPageIdsByDeletedAtIsNull(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(newestBookId, olderBookId), PageRequest.of(0, 2), 5));
        when(bookJpaRepository.findDistinctByDeletedAtIsNullAndIdIn(List.of(newestBookId, olderBookId)))
                .thenReturn(List.of(olderBookEntity, newestBookEntity));
        when(bookPersistenceMapper.toDomain(newestBookEntity)).thenReturn(newestBook);
        when(bookPersistenceMapper.toDomain(olderBookEntity)).thenReturn(olderBook);

        var result = bookRepositoryAdapter.findPageActive(0, 2);

        assertEquals(List.of(newestBook, olderBook), result.items());
        assertEquals(5, result.totalCount());
        verify(bookJpaRepository).findPageIdsByDeletedAtIsNull(PageRequest.of(0, 2));
        verify(bookJpaRepository).findDistinctByDeletedAtIsNullAndIdIn(List.of(newestBookId, olderBookId));
    }

    @Test
    void findRelatedActiveByCategoryId_preservesLimitedIdOrderAfterGraphFetch() {
        UUID categoryId = UUID.randomUUID();
        UUID excludedBookId = UUID.randomUUID();
        UUID firstRelatedBookId = UUID.randomUUID();
        UUID secondRelatedBookId = UUID.randomUUID();
        BookJpaEntity firstRelatedBookEntity = bookEntity(firstRelatedBookId);
        BookJpaEntity secondRelatedBookEntity = bookEntity(secondRelatedBookId);
        Book firstRelatedBook = org.mockito.Mockito.mock(Book.class);
        Book secondRelatedBook = org.mockito.Mockito.mock(Book.class);

        when(bookJpaRepository.findRelatedActiveIdsByCategoryId(categoryId, excludedBookId, PageRequest.of(0, 2)))
                .thenReturn(List.of(firstRelatedBookId, secondRelatedBookId));
        when(bookJpaRepository.findDistinctByDeletedAtIsNullAndIdIn(List.of(firstRelatedBookId, secondRelatedBookId)))
                .thenReturn(List.of(secondRelatedBookEntity, firstRelatedBookEntity));
        when(bookPersistenceMapper.toDomain(firstRelatedBookEntity)).thenReturn(firstRelatedBook);
        when(bookPersistenceMapper.toDomain(secondRelatedBookEntity)).thenReturn(secondRelatedBook);

        List<Book> result = bookRepositoryAdapter.findRelatedActiveByCategoryId(categoryId, excludedBookId, 2);

        assertEquals(List.of(firstRelatedBook, secondRelatedBook), result);
        verify(bookJpaRepository).findRelatedActiveIdsByCategoryId(categoryId, excludedBookId, PageRequest.of(0, 2));
        verify(bookJpaRepository).findDistinctByDeletedAtIsNullAndIdIn(List.of(firstRelatedBookId, secondRelatedBookId));
    }

    @Test
    void findAllByIdsIncludingDeletedForUpdate_locksIdsBeforeFetchingGraph() {
        UUID firstBookId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondBookId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        BookJpaEntity firstBookEntity = bookEntity(firstBookId);
        BookJpaEntity secondBookEntity = bookEntity(secondBookId);
        Book firstBook = org.mockito.Mockito.mock(Book.class);
        Book secondBook = org.mockito.Mockito.mock(Book.class);

        when(bookJpaRepository.findIdsByIdInForUpdate(List.of(firstBookId, secondBookId)))
                .thenReturn(List.of(firstBookId, secondBookId));
        when(bookJpaRepository.findAllByIdIn(List.of(firstBookId, secondBookId)))
                .thenReturn(List.of(secondBookEntity, firstBookEntity));
        when(bookPersistenceMapper.toDomain(firstBookEntity)).thenReturn(firstBook);
        when(bookPersistenceMapper.toDomain(secondBookEntity)).thenReturn(secondBook);

        List<Book> result = bookRepositoryAdapter.findAllByIdsIncludingDeletedForUpdate(List.of(secondBookId, firstBookId));

        assertEquals(List.of(firstBook, secondBook), result);
        verify(bookJpaRepository).findIdsByIdInForUpdate(List.of(firstBookId, secondBookId));
        verify(bookJpaRepository).findAllByIdIn(List.of(firstBookId, secondBookId));
    }

    private static BookJpaEntity bookEntity(UUID bookId) {
        BookJpaEntity entity = new BookJpaEntity();
        entity.setId(bookId);
        return entity;
    }
}
