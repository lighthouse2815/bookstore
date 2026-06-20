package com.bookstore.bookstore.infrastructure.persistence.projection.dashboard;

import java.util.UUID;

public interface LowStockBookProjection {

    UUID getBookId();

    String getTitle();

    Integer getStockQuantity();
}
