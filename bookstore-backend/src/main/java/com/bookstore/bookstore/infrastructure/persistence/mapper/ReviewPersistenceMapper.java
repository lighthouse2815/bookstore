package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewPersistenceMapper {

    public Review toDomain(ReviewJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Review(
                entity.getId(),
                entity.getUserId(),
                entity.getBookId(),
                entity.getOrderItemId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(Review review, ReviewJpaEntity entity) {
        entity.setId(review.getId());
        entity.setUserId(review.getUserId());
        entity.setBookId(review.getBookId());
        entity.setOrderItemId(review.getOrderItemId());
        entity.setRating(review.getRating());
        entity.setComment(review.getComment());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setUpdatedAt(review.getUpdatedAt());
        entity.setDeletedAt(review.getDeletedAt());
    }
}
