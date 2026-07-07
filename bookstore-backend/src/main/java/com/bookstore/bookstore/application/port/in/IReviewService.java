package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.DeleteReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReviewResult;
import java.util.List;
import java.util.UUID;

public interface IReviewService {

    List<ReviewResult> getByBookId(UUID bookId);

    PageSliceResult<ReviewResult> getByBookId(UUID bookId, int page, int size);

    ReviewResult create(CreateReviewCommand command);

    ReviewResult update(UpdateReviewCommand command);

    void delete(DeleteReviewCommand command);

    List<ReviewResult> getAll();

    PageSliceResult<ReviewResult> getAll(int page, int size);

    void adminDelete(UUID reviewId);
}
