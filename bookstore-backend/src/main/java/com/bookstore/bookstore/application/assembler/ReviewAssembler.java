package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewAssembler {

    public ReviewResult toResult(Review review) {
        return new ReviewResult(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                review.getOrderItemId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
