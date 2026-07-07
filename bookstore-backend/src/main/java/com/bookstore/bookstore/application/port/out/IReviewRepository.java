package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.Review;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IReviewRepository {

    List<Review> findAllByBookIdActive(UUID bookId);

    PageSliceResult<Review> findPageByBookIdActive(UUID bookId, int page, int size);

    Map<UUID, List<Integer>> findRatingsByBookIds(Collection<UUID> bookIds);

    List<Review> findAllActive();

    PageSliceResult<Review> findPageActive(int page, int size);

    Optional<Review> findByIdActive(UUID reviewId);

    Optional<Review> findByIdAndUserIdActive(UUID reviewId, UUID userId);

    Optional<Review> findByIdIncludingDeleted(UUID reviewId);

    boolean existsByOrderItemIdIncludingDeleted(UUID orderItemId);

    long countNewReviewsBetween(Instant fromInclusive, Instant toExclusive);

    Review save(Review review);
}
