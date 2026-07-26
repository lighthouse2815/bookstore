package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ReviewAssembler;
import com.bookstore.bookstore.application.command.ApproveReviewCommand;
import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.DeleteReviewCommand;
import com.bookstore.bookstore.application.command.HideReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IReviewService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.domain.rule.ReviewRule;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;
    private final ReviewAssembler reviewAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> getByBookId(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        requireActiveBook(bookId);

        return reviewRepository.findAllByBookIdActive(bookId).stream()
                .filter(Review::isPubliclyVisible)
                .map(reviewAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReviewResult> getByBookId(UUID bookId, int page, int size) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        validatePageRequest(page, size);
        requireActiveBook(bookId);
        return reviewRepository.findPageByBookIdActive(bookId, page, size).map(reviewAssembler::toResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult create(CreateReviewCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireActiveBook(command.bookId());

        if (reviewRepository.existsByOrderItemIdIncludingDeleted(command.orderItemId())) {
            throw new ApplicationException(ApplicationErrorCode.REVIEW_ALREADY_EXISTS);
        }

        requirePurchasedOrderItem(command.userId(), command.bookId(), command.orderItemId());

        Instant now = Instant.now();
        Review review = new Review(
                UUID.randomUUID(),
                command.userId(),
                command.bookId(),
                command.orderItemId(),
                command.rating(),
                StringUtils.trimToNull(command.comment()),
                ReviewStatus.APPROVED,
                null,
                null,
                null,
                now,
                now,
                null
        );

        return reviewAssembler.toResult(reviewRepository.save(review));
    }

    private void requireActiveBook(UUID bookId) {
        if (bookRepository.findByIdActive(bookId).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult update(UpdateReviewCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Review currentReview = reviewRepository.findByIdAndUserIdActive(command.reviewId(), command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        requireActiveBook(currentReview.getBookId());
        currentReview.updateReview(command.rating(), StringUtils.trimToNull(command.comment()));
        return reviewAssembler.toResult(reviewRepository.save(currentReview));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteReviewCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Review currentReview = reviewRepository.findByIdAndUserIdActive(command.reviewId(), command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        currentReview.softDelete();
        reviewRepository.save(currentReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> getAll() {
        return reviewRepository.findAllActive().stream()
                .map(reviewAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReviewResult> getAll(int page, int size) {
        validatePageRequest(page, size);
        return reviewRepository.findPageActive(page, size).map(reviewAssembler::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReviewResult> getAll(
            int page,
            int size,
            ReviewStatus status,
            UUID bookId,
            UUID userId,
            Integer rating
    ) {
        validatePageRequest(page, size);
        validateRatingFilter(rating);
        return reviewRepository.findPageActive(page, size, status, bookId, userId, rating)
                .map(reviewAssembler::toResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult hide(HideReviewCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Review currentReview = reviewRepository.findByIdActive(command.reviewId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        currentReview.hide(StringUtils.trimToNull(command.reason()), command.adminUserId());
        return reviewAssembler.toResult(reviewRepository.save(currentReview));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult approve(ApproveReviewCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Review currentReview = reviewRepository.findByIdActive(command.reviewId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        currentReview.approve(command.adminUserId());
        return reviewAssembler.toResult(reviewRepository.save(currentReview));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult adminDelete(UUID reviewId) {
        if (reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "reviewId");
        }

        Review currentReview = reviewRepository.findByIdActive(reviewId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        currentReview.softDelete();
        return reviewAssembler.toResult(reviewRepository.save(currentReview));
    }

    private void requirePurchasedOrderItem(UUID userId, UUID bookId, UUID orderItemId) {
        boolean purchased = orderRepository.findByUserId(userId).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getItems)
                .flatMap(List::stream)
                .anyMatch(orderItem -> orderItemMatches(orderItem, bookId, orderItemId));

        if (!purchased) {
            throw new ApplicationException(ApplicationErrorCode.REVIEW_BOOK_NOT_PURCHASED);
        }
    }

    private boolean orderItemMatches(OrderItem orderItem, UUID bookId, UUID orderItemId) {
        return orderItem.getId().equals(orderItemId) && orderItem.getBookId().equals(bookId);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }

        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }

    private void validateRatingFilter(Integer rating) {
        if (rating != null) {
            ReviewRule.requireValidRating(rating);
        }
    }
}
