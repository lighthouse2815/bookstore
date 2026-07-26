package com.bookstore.bookstore.infrastructure.persistence.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface TopBookStatsProjection {

    UUID getBookId();

    String getTitle();

    Long getSoldQuantity();

    BigDecimal getRevenue();
}
