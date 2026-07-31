package com.bookstore.bookstore.application.query;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record PageQuery(
        int page,
        int size
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 20;

    public PageQuery {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new ApplicationException(ApplicationErrorCode.PAGINATION_INVALID);
        }
    }
}
