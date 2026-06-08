package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ReviewPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ReviewJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements IReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewPersistenceMapper reviewPersistenceMapper;

    @Override
    public List<Review> findAllByBookIdActive(UUID bookId) {
        return reviewJpaRepository.findAllByBookIdActive(bookId).stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Review> findAllActive() {
        return reviewJpaRepository.findAllActive().stream()
                .map(reviewPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Review> findByIdActive(UUID reviewId) {
        return reviewJpaRepository.findByIdActive(reviewId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdAndUserIdActive(UUID reviewId, UUID userId) {
        return reviewJpaRepository.findByIdAndUserIdActive(reviewId, userId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdIncludingDeleted(UUID reviewId) {
        return reviewJpaRepository.findByIdIncludingDeleted(reviewId)
                .map(reviewPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByOrderItemIdIncludingDeleted(UUID orderItemId) {
        return reviewJpaRepository.existsByOrderItemIdIncludingDeleted(orderItemId);
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = reviewJpaRepository.findByIdIncludingDeleted(review.getId())
                .orElseGet(ReviewJpaEntity::new);
        reviewPersistenceMapper.copyToEntity(review, entity);
        return reviewPersistenceMapper.toDomain(reviewJpaRepository.save(entity));
    }
}
