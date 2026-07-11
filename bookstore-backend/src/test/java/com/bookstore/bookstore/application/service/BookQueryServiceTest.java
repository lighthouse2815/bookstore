package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Publisher;
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
class BookQueryServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IAuthorRepository authorRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IPublisherRepository publisherRepository;

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ICouponService couponService;

    @InjectMocks
    private BookQueryService bookQueryService;

    @Test
    void getById_buildsRatingSummaryFromVisibleRatings() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(reviewRepository.findRatingsByBookIds(List.of(bookId))).thenReturn(
                Map.of(bookId, List.of(5, 4))
        );
        when(orderRepository.countDeliveredQuantityByBookIds(List.of(bookId))).thenReturn(
                Map.of(bookId, 3L)
        );

        BookQueryResult result = bookQueryService.getById(bookId);

        assertEquals(new BigDecimal("4.5"), result.ratingSummary().averageRating());
        assertEquals(2L, result.ratingSummary().reviewCount());
        assertEquals(1L, result.ratingSummary().starBreakdown().get(5));
        assertEquals(1L, result.ratingSummary().starBreakdown().get(4));
        assertEquals(0L, result.ratingSummary().starBreakdown().get(3));
        assertEquals(3L, result.soldCount());
    }

    @Test
    void getPageDetail_includesSoftDeletedBookReferences() {
        UUID bookId = UUID.randomUUID();
        Book book = book(bookId);
        Instant deletedAt = Instant.now().minusSeconds(60);
        Author deletedAuthor = new Author(
                book.getAuthorId(), "Author", "Biography", null, null, null,
                deletedAt, deletedAt, deletedAt
        );
        Publisher deletedPublisher = new Publisher(
                book.getPublisherId(), "Publisher", "Description", null,
                deletedAt, deletedAt, deletedAt
        );
        Category deletedCategory = new Category(
                book.getCategoryId(), "Category", "Description", null, null,
                deletedAt, deletedAt, deletedAt
        );
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.findRatingsByBookIds(List.of(bookId))).thenReturn(Map.of());
        when(orderRepository.countDeliveredQuantityByBookIds(List.of(bookId))).thenReturn(Map.of());
        when(authorRepository.findByIdIncludingDeleted(book.getAuthorId())).thenReturn(Optional.of(deletedAuthor));
        when(publisherRepository.findByIdIncludingDeleted(book.getPublisherId())).thenReturn(Optional.of(deletedPublisher));
        when(categoryRepository.findByIdIncludingDeleted(book.getCategoryId())).thenReturn(Optional.of(deletedCategory));
        when(couponService.getPublicActivePromotions(org.mockito.ArgumentMatchers.any(Instant.class))).thenReturn(List.of());
        when(bookRepository.findRelatedActiveByCategoryId(book.getCategoryId(), bookId, 8)).thenReturn(List.of());

        var result = bookQueryService.getPageDetail(bookId, 8);

        assertEquals(deletedAuthor, result.author());
        assertEquals(deletedPublisher, result.publisher());
        assertEquals(List.of(deletedCategory), result.categoryTrail());
        verify(authorRepository).findByIdIncludingDeleted(book.getAuthorId());
        verify(publisherRepository).findByIdIncludingDeleted(book.getPublisherId());
        verify(categoryRepository).findByIdIncludingDeleted(book.getCategoryId());
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("10.00"),
                10,
                List.of(new BookImage(
                        UUID.randomUUID(),
                        bookId,
                        fileAsset(),
                        true,
                        0,
                        null,
                        now
                )),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static FileAsset fileAsset() {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                UUID.randomUUID(),
                FileProvider.R2,
                FilePurpose.BOOK_IMAGE,
                "bookstore-assets",
                "public/books/cover.jpg",
                "https://cdn.example.com/public/books/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1024L,
                "checksum",
                FileVisibility.PUBLIC,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
