package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ReviewAssembler;
import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Review;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private ReviewAssembler reviewAssembler;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void create_whenDeliveredOrderItemExists_savesReview() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        Book book = book(bookId);
        Order deliveredOrder = deliveredOrder(userId, bookId, orderItemId);
        ReviewResult expected = new ReviewResult(
                UUID.randomUUID(),
                userId,
                bookId,
                orderItemId,
                "reviewer",
                null,
                true,
                List.of(),
                0L,
                5,
                "Great book",
                Instant.EPOCH,
                Instant.EPOCH
        );

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByOrderItemIdIncludingDeleted(orderItemId)).thenReturn(false);
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(deliveredOrder));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewAssembler.toResult(any(Review.class))).thenReturn(expected);

        ReviewResult result = reviewService.create(
                new CreateReviewCommand(userId, bookId, orderItemId, 5, "Great book")
        );

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(bookId, captor.getValue().getBookId());
        assertEquals(orderItemId, captor.getValue().getOrderItemId());
        assertEquals(expected, result);
    }

    @Test
    void create_whenOrderItemAlreadyReviewed_rejectsConflict() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(reviewRepository.existsByOrderItemIdIncludingDeleted(orderItemId)).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> reviewService.create(new CreateReviewCommand(userId, bookId, orderItemId, 5, "Great book"))
        );

        assertEquals(ApplicationErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void create_whenBookWasNotPurchased_rejectsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(reviewRepository.existsByOrderItemIdIncludingDeleted(orderItemId)).thenReturn(false);
        when(orderRepository.findByUserId(userId)).thenReturn(List.of());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> reviewService.create(new CreateReviewCommand(userId, bookId, orderItemId, 5, "Great book"))
        );

        assertEquals(ApplicationErrorCode.REVIEW_BOOK_NOT_PURCHASED, exception.getErrorCode());
    }

    @Test
    void create_whenBookSoftDeleted_rejectsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> reviewService.create(new CreateReviewCommand(userId, bookId, orderItemId, 5, "Great book"))
        );

        assertEquals(ApplicationErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void update_whenBookSoftDeleted_rejectsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        Instant now = Instant.EPOCH;
        Review review = new Review(
                UUID.randomUUID(),
                userId,
                bookId,
                orderItemId,
                4,
                "Old review",
                now,
                now,
                null
        );

        when(reviewRepository.findByIdAndUserIdActive(review.getId(), userId)).thenReturn(Optional.of(review));
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> reviewService.update(new UpdateReviewCommand(review.getId(), userId, 5, "Updated review"))
        );

        assertEquals(ApplicationErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
        verify(reviewRepository, never()).save(any(Review.class));
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

    private static com.bookstore.bookstore.domain.model.FileAsset fileAsset() {
        Instant now = Instant.EPOCH;
        return new com.bookstore.bookstore.domain.model.FileAsset(
                UUID.randomUUID(),
                com.bookstore.bookstore.domain.enums.FileProvider.R2,
                com.bookstore.bookstore.domain.enums.FilePurpose.BOOK_IMAGE,
                "bookstore-assets",
                "public/books/cover.jpg",
                "https://cdn.example.com/public/books/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1024L,
                "checksum",
                com.bookstore.bookstore.domain.enums.FileVisibility.PUBLIC,
                com.bookstore.bookstore.domain.enums.FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static Order deliveredOrder(UUID userId, UUID bookId, UUID orderItemId) {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                orderItemId,
                bookId,
                "Book Title",
                new BigDecimal("10.00"),
                1,
                new BigDecimal("10.00")
        );

        return new Order(
                UUID.randomUUID(),
                userId,
                List.of(item),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.DELIVERED,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }
}
