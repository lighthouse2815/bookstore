package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ReviewAssembler;
import com.bookstore.bookstore.application.command.ApproveReviewCommand;
import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.HideReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.FileAsset;
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
    void create_whenDeliveredOrderItemExists_savesApprovedReview() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        Book book = book(bookId);
        Order deliveredOrder = deliveredOrder(userId, bookId, orderItemId);

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByOrderItemIdIncludingDeleted(orderItemId)).thenReturn(false);
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(deliveredOrder));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewAssembler.toResult(any(Review.class))).thenAnswer(
                invocation -> reviewResult(invocation.getArgument(0))
        );

        ReviewResult result = reviewService.create(
                new CreateReviewCommand(userId, bookId, orderItemId, 5, "Great book")
        );

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(bookId, captor.getValue().getBookId());
        assertEquals(orderItemId, captor.getValue().getOrderItemId());
        assertEquals(ReviewStatus.APPROVED, captor.getValue().getStatus());
        assertEquals(ReviewStatus.APPROVED, result.status());
        assertEquals("Great book", result.comment());
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
                ReviewStatus.APPROVED,
                null,
                null,
                null,
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

    @Test
    void getByBookId_filtersOutHiddenReviews() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review approvedReview = review(
                UUID.randomUUID(),
                userId,
                bookId,
                UUID.randomUUID(),
                5,
                "Visible review",
                ReviewStatus.APPROVED,
                null,
                null,
                null,
                null
        );
        Review hiddenReview = review(
                UUID.randomUUID(),
                userId,
                bookId,
                UUID.randomUUID(),
                2,
                "Hidden review",
                ReviewStatus.HIDDEN,
                "Spam",
                UUID.randomUUID(),
                Instant.EPOCH,
                null
        );

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(reviewRepository.findAllByBookIdActive(bookId)).thenReturn(List.of(approvedReview, hiddenReview));
        when(reviewAssembler.toResult(any(Review.class))).thenAnswer(
                invocation -> reviewResult(invocation.getArgument(0))
        );

        List<ReviewResult> results = reviewService.getByBookId(bookId);

        assertEquals(1, results.size());
        assertEquals(approvedReview.getId(), results.getFirst().reviewId());
        verify(reviewAssembler, never()).toResult(same(hiddenReview));
    }

    @Test
    void hide_whenReviewExists_updatesStatusAndModerationMetadata() {
        UUID reviewId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        Review review = review(
                reviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                "Old review",
                ReviewStatus.APPROVED,
                null,
                null,
                null,
                null
        );

        when(reviewRepository.findByIdActive(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewAssembler.toResult(any(Review.class))).thenAnswer(
                invocation -> reviewResult(invocation.getArgument(0))
        );

        ReviewResult result = reviewService.hide(new HideReviewCommand(reviewId, adminUserId, "Noi dung khong phu hop"));

        assertEquals(ReviewStatus.HIDDEN, result.status());
        assertEquals("Noi dung khong phu hop", result.moderationReason());
        assertEquals(adminUserId, result.moderatedBy());
        assertNotNull(result.moderatedAt());
    }

    @Test
    void approve_whenHiddenReviewExists_clearsReasonAndMarksReviewApproved() {
        UUID reviewId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        Review review = review(
                reviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                "Old review",
                ReviewStatus.HIDDEN,
                "Noi dung khong phu hop",
                UUID.randomUUID(),
                Instant.EPOCH,
                null
        );

        when(reviewRepository.findByIdActive(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewAssembler.toResult(any(Review.class))).thenAnswer(
                invocation -> reviewResult(invocation.getArgument(0))
        );

        ReviewResult result = reviewService.approve(new ApproveReviewCommand(reviewId, adminUserId));

        assertEquals(ReviewStatus.APPROVED, result.status());
        assertEquals(null, result.moderationReason());
        assertEquals(adminUserId, result.moderatedBy());
        assertNotNull(result.moderatedAt());
    }

    @Test
    void adminDelete_whenReviewExists_softDeletesReview() {
        UUID reviewId = UUID.randomUUID();
        Review review = review(
                reviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                4,
                "Old review",
                ReviewStatus.APPROVED,
                null,
                null,
                null,
                null
        );

        when(reviewRepository.findByIdActive(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewAssembler.toResult(any(Review.class))).thenAnswer(
                invocation -> reviewResult(invocation.getArgument(0))
        );

        ReviewResult result = reviewService.adminDelete(reviewId);

        assertEquals(reviewId, result.reviewId());
        assertNotNull(review.getDeletedAt());
        verify(reviewRepository).save(review);
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

    private static Review review(
            UUID reviewId,
            UUID userId,
            UUID bookId,
            UUID orderItemId,
            int rating,
            String comment,
            ReviewStatus status,
            String moderationReason,
            UUID moderatedBy,
            Instant moderatedAt,
            Instant deletedAt
    ) {
        Instant now = Instant.EPOCH;
        return new Review(
                reviewId,
                userId,
                bookId,
                orderItemId,
                rating,
                comment,
                status,
                moderationReason,
                moderatedBy,
                moderatedAt,
                now,
                now,
                deletedAt
        );
    }

    private static ReviewResult reviewResult(Review review) {
        return new ReviewResult(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                review.getOrderItemId(),
                "reviewer",
                null,
                true,
                List.of(),
                0L,
                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getModerationReason(),
                review.getModeratedBy(),
                null,
                review.getModeratedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
