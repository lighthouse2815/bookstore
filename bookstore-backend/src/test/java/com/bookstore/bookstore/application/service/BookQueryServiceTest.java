package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.bookstore.bookstore.domain.model.FileAsset;
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
