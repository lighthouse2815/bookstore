package com.bookstore.bookstore.application.query;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageQueryTest {

    @Test
    void constructor_acceptsValidPagination() {
        PageQuery query = new PageQuery(0, PageQuery.MAX_SIZE);

        assertEquals(0, query.page());
        assertEquals(20, query.size());
    }

    @Test
    void constructor_rejectsInvalidPagination() {
        assertPaginationInvalid(-1, 20);
        assertPaginationInvalid(0, 0);
        assertPaginationInvalid(0, 21);
    }

    private void assertPaginationInvalid(int page, int size) {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> new PageQuery(page, size)
        );

        assertEquals(ApplicationErrorCode.PAGINATION_INVALID, exception.getErrorCode());
    }
}
