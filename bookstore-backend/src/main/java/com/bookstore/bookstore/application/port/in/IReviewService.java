package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateReviewCommand;
import com.bookstore.bookstore.application.command.DeleteReviewCommand;
import com.bookstore.bookstore.application.command.UpdateReviewCommand;
import com.bookstore.bookstore.application.result.ReviewResult;
import java.util.List;
import java.util.UUID;

public interface IReviewService {

    List<ReviewResult> getByBookId(UUID bookId);

    ReviewResult create(CreateReviewCommand command);

    ReviewResult update(UpdateReviewCommand command);

    void delete(DeleteReviewCommand command);

    List<ReviewResult> getAll();

    void adminDelete(UUID reviewId);
}
