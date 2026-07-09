package com.bookstore.bookstore.infrastructure.persistence.projection;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
import java.time.Instant;

public interface ReviewReportProjection {

    String getBookTitle();

    String getUsername();

    Integer getRating();

    ReviewStatus getStatus();

    Instant getCreatedAt();

    String getModerationReason();
}
