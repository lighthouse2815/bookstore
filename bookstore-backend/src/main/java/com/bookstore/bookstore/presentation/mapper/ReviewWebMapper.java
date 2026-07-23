package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.ApproveReviewCommand;
import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.DeleteReviewCommand;
import com.bookstore.bookstore.application.command.HideReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.presentation.request.CreateReviewRequest;
import com.bookstore.bookstore.presentation.request.ReviewModerationRequest;
import com.bookstore.bookstore.presentation.request.UpdateReviewRequest;
import com.bookstore.bookstore.presentation.response.PublicReviewResponse;
import com.bookstore.bookstore.presentation.response.ReviewResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReviewWebMapper {

    public CreateReviewCommand toCreateCommand(UUID userId, UUID bookId, CreateReviewRequest request) {
        return new CreateReviewCommand(
                userId,
                bookId,
                request.orderItemId(),
                request.rating(),
                request.comment()
        );
    }

    public UpdateReviewCommand toUpdateCommand(UUID reviewId, UUID userId, UpdateReviewRequest request) {
        return new UpdateReviewCommand(
                reviewId,
                userId,
                request.rating(),
                request.comment()
        );
    }

    public DeleteReviewCommand toDeleteCommand(UUID reviewId, UUID userId) {
        return new DeleteReviewCommand(reviewId, userId);
    }

    public HideReviewCommand toHideCommand(UUID reviewId, UUID adminUserId, ReviewModerationRequest request) {
        return new HideReviewCommand(
                reviewId,
                adminUserId,
                request == null ? null : request.reason()
        );
    }

    public ApproveReviewCommand toApproveCommand(UUID reviewId, UUID adminUserId) {
        return new ApproveReviewCommand(reviewId, adminUserId);
    }

    public ReviewResponse toResponse(ReviewResult result) {
        return new ReviewResponse(
                result.reviewId(),
                result.userId(),
                result.bookId(),
                result.orderItemId(),
                result.reviewerName(),
                result.reviewerAvatarUrl(),
                result.verifiedPurchase(),
                result.reviewImages(),
                result.helpfulCount(),
                result.rating(),
                result.comment(),
                result.status(),
                result.moderationReason(),
                result.moderatedBy(),
                result.moderatedByName(),
                result.moderatedAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public PublicReviewResponse toPublicResponse(ReviewResult result) {
        return new PublicReviewResponse(
                result.reviewId(),
                result.reviewerName(),
                result.reviewerAvatarUrl(),
                result.verifiedPurchase(),
                result.reviewImages(),
                result.helpfulCount(),
                result.rating(),
                result.comment(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
