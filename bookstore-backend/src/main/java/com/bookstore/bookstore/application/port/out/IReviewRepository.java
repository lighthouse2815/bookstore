package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReviewRepository {

    List<Review> findAllByBookIdActive(UUID bookId);

    List<Review> findAllActive();

    Optional<Review> findByIdActive(UUID reviewId);

    Optional<Review> findByIdAndUserIdActive(UUID reviewId, UUID userId);

    Optional<Review> findByIdIncludingDeleted(UUID reviewId);

    boolean existsByOrderItemIdIncludingDeleted(UUID orderItemId);

    Review save(Review review);
}
