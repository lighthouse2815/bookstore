package com.bookstore.bookstore.infrastructure.persistence.projection;

import java.util.UUID;

public interface LowStockReportProjection {

    UUID getBookId();

    String getTitle();

    String getIsbn();

    Integer getStockQuantity();

    String getCategoryName();
}
