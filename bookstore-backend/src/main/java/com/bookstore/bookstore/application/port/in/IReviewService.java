package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.ApproveReviewCommand;
import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.DeleteReviewCommand;
import com.bookstore.bookstore.application.command.HideReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReviewResult;
import java.util.List;
import java.util.UUID;

public interface IReviewService {

    List<ReviewResult> getByBookId(UUID bookId);

    PageSliceResult<ReviewResult> getByBookId(UUID bookId, PageQuery pageQuery);

    ReviewResult create(CreateReviewCommand command);

    ReviewResult update(UpdateReviewCommand command);

    void delete(DeleteReviewCommand command);

    List<ReviewResult> getAll();

    PageSliceResult<ReviewResult> getAll(PageQuery pageQuery);

    PageSliceResult<ReviewResult> getAll(
            PageQuery pageQuery,
            ReviewStatus status,
            UUID bookId,
            UUID userId,
            Integer rating
    );

    ReviewResult hide(HideReviewCommand command);

    ReviewResult approve(ApproveReviewCommand command);

    ReviewResult adminDelete(UUID reviewId);
}
