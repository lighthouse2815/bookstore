package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IWishlistRepository;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.WishlistItem;
import java.math.BigDecimal;
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
class WishlistServiceTest {

    @Mock
    private IWishlistRepository wishlistRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void getMyWishlist_returnsEnrichedBooks() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        WishlistItem wishlistItem = wishlistItem(userId, bookId, false);
        Book book = book(bookId, new BigDecimal("120000"));

        when(wishlistRepository.findAllByUserIdActive(userId)).thenReturn(List.of(wishlistItem));
        when(bookRepository.findAllByIdsActive(List.of(bookId))).thenReturn(List.of(book));
        when(reviewRepository.findRatingsByBookIds(List.of(bookId))).thenReturn(Map.of(bookId, List.of(5, 4)));
        when(orderRepository.countDeliveredQuantityByBookIds(List.of(bookId))).thenReturn(Map.of(bookId, 7L));

        var result = wishlistService.getMyWishlist(userId);

        assertEquals(1, result.size());
        assertEquals(bookId, result.get(0).book().getId());
        assertEquals(7L, result.get(0).soldCount());
        assertEquals(new BigDecimal("4.5"), result.get(0).ratingSummary().averageRating());
        assertEquals(2L, result.get(0).ratingSummary().reviewCount());
    }

    @Test
    void addBook_whenDeletedWishlistItemExists_restoresExistingItem() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Book book = book(bookId, new BigDecimal("99000"));
        WishlistItem deletedItem = wishlistItem(userId, bookId, true);

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book));
        when(wishlistRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.of(deletedItem));

        wishlistService.addBook(userId, bookId);

        assertFalse(deletedItem.isDeleted());
        verify(wishlistRepository).save(deletedItem);
    }

    @Test
    void removeBook_whenActiveWishlistItemExists_softDeletesItem() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        WishlistItem activeItem = wishlistItem(userId, bookId, false);

        when(wishlistRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.of(activeItem));

        wishlistService.removeBook(userId, bookId);

        assertTrue(activeItem.isDeleted());
        verify(wishlistRepository).save(activeItem);
        verifyNoMoreInteractions(bookRepository);
    }

    private static WishlistItem wishlistItem(UUID userId, UUID bookId, boolean deleted) {
        Instant now = Instant.EPOCH;
        return new WishlistItem(
                UUID.randomUUID(),
                userId,
                bookId,
                now,
                now,
                deleted ? now.plusSeconds(1) : null
        );
    }

    private static Book book(UUID bookId, BigDecimal price) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Book title",
                "ISBN-001",
                "Description",
                price,
                10,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
