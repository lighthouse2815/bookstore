package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Review;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IReviewRepository {

    List<Review> findAllByBookIdActive(UUID bookId);

    Map<UUID, List<Integer>> findRatingsByBookIds(Collection<UUID> bookIds);

    List<Review> findAllActive();

    Optional<Review> findByIdActive(UUID reviewId);

    Optional<Review> findByIdAndUserIdActive(UUID reviewId, UUID userId);

    Optional<Review> findByIdIncludingDeleted(UUID reviewId);

    boolean existsByOrderItemIdIncludingDeleted(UUID orderItemId);

    long countNewReviewsBetween(Instant fromInclusive, Instant toExclusive);

    Review save(Review review);
}
