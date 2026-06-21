package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReviewRepository;
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
        return reviewJpaRepository.findAllByBook_IdActive(bookId).stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, List<Integer>> findRatingsByBookIds(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        return reviewJpaRepository.findRatingsByBookIds(bookIds).stream()
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
