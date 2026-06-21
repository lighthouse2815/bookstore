package com.bookstore.bookstore.domain.enums;

public enum CouponTargetType {
    ALL_ORDER,   // áp dụng toàn đơn
    BOOK,        // áp dụng cho sách cụ thể
    CATEGORY,    // áp dụng cho danh mục cụ thể
    AUTHOR,      // nếu muốn áp dụng theo tác giả
    PUBLISHER    // nếu muốn áp dụng theo nhà xuất bản
}
