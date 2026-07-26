package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReviewReportRowResult;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ReviewPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ReviewJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements IReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final ReviewPersistenceMapper reviewPersistenceMapper;

    @Override
    public List<Review> findAllByBookIdActive(UUID bookId) {
        return reviewJpaRepository.findAllByBook_IdAndStatusActive(bookId, ReviewStatus.APPROVED).stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Review> findPageByBookIdActive(UUID bookId, int page, int size) {
        var resultPage = reviewJpaRepository.findAllByBook_IdAndStatusActive(
                bookId,
                ReviewStatus.APPROVED,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.stream()
                        .map(reviewPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Map<UUID, List<Integer>> findRatingsByBookIds(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        return reviewJpaRepository.findRatingsByBookIdsAndStatus(bookIds, ReviewStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(
                        row -> (UUID) row[0],
                        Collectors.mapping(row -> (Integer) row[1], Collectors.toList())
                ));
    }

    @Override
    public List<Review> findAllActive() {
        return reviewJpaRepository.findAllByDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Review> findAllByUserIdActive(UUID userId) {
        return reviewJpaRepository.findAllByUserIdActive(userId).stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Review> findPageActive(int page, int size) {
        var resultPage = reviewJpaRepository
                .findAllByDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNullOrderByCreatedAtDesc(
                        PageRequest.of(page, size)
                );
        return new PageSliceResult<>(
                resultPage.stream()
                        .map(reviewPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageSliceResult<Review> findPageActive(
            int page,
            int size,
            ReviewStatus status,
            UUID bookId,
            UUID userId,
            Integer rating
    ) {
        var resultPage = reviewJpaRepository.findPageActive(
                status,
                bookId,
                userId,
                rating,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.stream()
                        .map(reviewPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Optional<Review> findByIdActive(UUID reviewId) {
        return reviewJpaRepository.findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNull(reviewId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdAndUserIdActive(UUID reviewId, UUID userId) {
        return reviewJpaRepository.findByIdAndUser_IdAndDeletedAtIsNullAndUser_DeletedAtIsNull(reviewId, userId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdIncludingDeleted(UUID reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByOrderItemIdIncludingDeleted(UUID orderItemId) {
        return reviewJpaRepository.existsByOrderItemId(orderItemId);
    }

    @Override
    public long countNewReviewsBetween(Instant fromInclusive, Instant toExclusive) {
        return reviewJpaRepository.countNewReviewsBetween(fromInclusive, toExclusive);
    }

    @Override
    public List<ReviewReportRowResult> findReviewReportRows(ReviewStatus status) {
        return reviewJpaRepository.findReviewReportRows(status).stream()
                .map(row -> new ReviewReportRowResult(
                        row.getBookTitle(),
                        row.getUsername(),
                        row.getRating() == null ? 0 : row.getRating(),
                        row.getStatus(),
                        row.getCreatedAt(),
                        row.getModerationReason()
                ))
                .toList();
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = reviewJpaRepository.findById(review.getId())
                .orElseGet(ReviewJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(review.getUserId());
        BookJpaEntity book = bookJpaRepository.getReferenceById(review.getBookId());
        OrderItemJpaEntity orderItem = orderItemJpaRepository.getReferenceById(review.getOrderItemId());
        
        reviewPersistenceMapper.copyToEntity(review, entity, user, book, orderItem);
        return reviewPersistenceMapper.toDomain(reviewJpaRepository.save(entity));
    }
}
