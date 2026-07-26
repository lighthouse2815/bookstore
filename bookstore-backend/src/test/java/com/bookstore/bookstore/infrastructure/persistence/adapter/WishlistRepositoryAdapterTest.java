package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.WishlistItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.WishlistItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.WishlistItemPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.WishlistItemJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishlistRepositoryAdapterTest {

    @Mock
    private WishlistItemJpaRepository wishlistItemJpaRepository;

    @Mock
    private WishlistItemPersistenceMapper wishlistItemPersistenceMapper;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @InjectMocks
    private WishlistRepositoryAdapter wishlistRepositoryAdapter;

    @Test
    void findAllByUserIdActive_mapsJpaEntitiesToDomainItems() {
        UUID userId = UUID.randomUUID();
        WishlistItemJpaEntity entity = new WishlistItemJpaEntity();
        WishlistItem expected = wishlistItem(userId, UUID.randomUUID());

        when(wishlistItemJpaRepository.findAllByUserIdActive(userId)).thenReturn(List.of(entity));
        when(wishlistItemPersistenceMapper.toDomain(entity)).thenReturn(expected);

        List<WishlistItem> result = wishlistRepositoryAdapter.findAllByUserIdActive(userId);

        assertEquals(List.of(expected), result);
        verify(wishlistItemJpaRepository).findAllByUserIdActive(userId);
    }

    @Test
    void save_reusesExistingEntityAndResolvesUserAndBookReferences() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        WishlistItem wishlistItem = wishlistItem(userId, bookId);
        WishlistItemJpaEntity entity = new WishlistItemJpaEntity();
        UserJpaEntity user = new UserJpaEntity();
        user.setId(userId);
        BookJpaEntity book = new BookJpaEntity();
        book.setId(bookId);

        when(wishlistItemJpaRepository.findById(wishlistItem.getId())).thenReturn(Optional.of(entity));
        when(userJpaRepository.getReferenceById(userId)).thenReturn(user);
        when(bookJpaRepository.getReferenceById(bookId)).thenReturn(book);
        when(wishlistItemJpaRepository.save(entity)).thenReturn(entity);
        when(wishlistItemPersistenceMapper.toDomain(entity)).thenReturn(wishlistItem);

        WishlistItem result = wishlistRepositoryAdapter.save(wishlistItem);

        assertSame(wishlistItem, result);
        verify(wishlistItemPersistenceMapper).copyToEntity(wishlistItem, entity, user, book);
        verify(wishlistItemJpaRepository).save(entity);
    }

    private static WishlistItem wishlistItem(UUID userId, UUID bookId) {
        Instant now = Instant.EPOCH;
        return new WishlistItem(
                UUID.randomUUID(),
                userId,
                bookId,
                now,
                now,
                null
        );
    }
}
