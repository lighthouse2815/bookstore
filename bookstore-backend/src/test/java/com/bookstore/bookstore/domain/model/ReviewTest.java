package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewTest {

    @Test
    void constructor_rejectsRatingOutsideRange() {
        Instant now = Instant.EPOCH;

        DomainException exception = assertThrows(
                DomainException.class,
                () -> new Review(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        0,
                        "comment",
                        now,
                        now,
                        null
                )
        );

        assertEquals(DomainErrorCode.INVALID_REVIEW_RATING, exception.getErrorCode());
    }

    @Test
    void updateReview_rejectsDeletedReview() {
        Instant now = Instant.EPOCH;
        Review review = new Review(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                5,
                "comment",
                now,
                now,
                now
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> review.updateReview(4, "updated")
        );

        assertEquals(DomainErrorCode.DELETED_REVIEW_CANNOT_UPDATE, exception.getErrorCode());
    }
}
