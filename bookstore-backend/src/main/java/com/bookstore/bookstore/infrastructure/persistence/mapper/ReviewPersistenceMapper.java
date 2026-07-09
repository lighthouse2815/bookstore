package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewPersistenceMapper {

    public Review toDomain(ReviewJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Review(
                entity.getId(),
                entity.getUser().getId(),
                entity.getBook().getId(),
                entity.getOrderItem().getId(),
                entity.getRating(),
                entity.getComment(),
                entity.getStatus(),
                entity.getModerationReason(),
                entity.getModeratedBy(),
                entity.getModeratedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            Review review,
            ReviewJpaEntity entity,
            UserJpaEntity user,
            BookJpaEntity book,
            OrderItemJpaEntity orderItem
    ) {
        entity.setId(review.getId());
        entity.setUser(user);
        entity.setBook(book);
        entity.setOrderItem(orderItem);
        entity.setRating(review.getRating());
        entity.setComment(review.getComment());
        entity.setStatus(review.getStatus());
        entity.setModerationReason(review.getModerationReason());
        entity.setModeratedBy(review.getModeratedBy());
        entity.setModeratedAt(review.getModeratedAt());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setUpdatedAt(review.getUpdatedAt());
        entity.setDeletedAt(review.getDeletedAt());
    }
}
