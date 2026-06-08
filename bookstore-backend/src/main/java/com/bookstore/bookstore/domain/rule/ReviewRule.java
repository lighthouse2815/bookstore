package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class ReviewRule {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;

    private ReviewRule() {
    }

    public static void requireValidRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new DomainException(DomainErrorCode.INVALID_REVIEW_RATING, "rating");
        }
    }

    public static void requireValidCommentLength(String comment) {
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new DomainException(DomainErrorCode.INVALID_REVIEW_COMMENT, "comment");
        }
    }

    public static void requireCanUpdate(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_REVIEW_CANNOT_UPDATE);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.REVIEW_ALREADY_DELETED);
        }
    }
}
