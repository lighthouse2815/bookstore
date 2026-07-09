package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
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
                        ReviewStatus.APPROVED,
                        null,
                        null,
                        null,
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
                ReviewStatus.APPROVED,
                null,
                null,
                null,
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

    @Test
    void approve_clearsModerationReasonAndMarksReviewVisible() {
        Instant now = Instant.EPOCH;
        UUID adminUserId = UUID.randomUUID();
        Review review = new Review(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                5,
                "comment",
                ReviewStatus.HIDDEN,
                "Noi dung khong phu hop",
                UUID.randomUUID(),
                now,
                now,
                now,
                null
        );

        review.approve(adminUserId);

        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        assertNull(review.getModerationReason());
        assertEquals(adminUserId, review.getModeratedBy());
        assertTrue(review.isPubliclyVisible());
    }
}
