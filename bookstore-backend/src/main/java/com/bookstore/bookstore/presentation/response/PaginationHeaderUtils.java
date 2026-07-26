package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.application.result.PageSliceResult;
import org.springframework.http.HttpHeaders;

public final class PaginationHeaderUtils {

    private static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_PAGE = "X-Page";
    private static final String HEADER_SIZE = "X-Size";
    private static final String HEADER_HAS_NEXT = "X-Has-Next";

    private PaginationHeaderUtils() {
    }

    public static HttpHeaders build(PageSliceResult<?> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_TOTAL_COUNT, String.valueOf(result.totalCount()));
        headers.add(HEADER_PAGE, String.valueOf(result.page()));
        headers.add(HEADER_SIZE, String.valueOf(result.size()));
        headers.add(HEADER_HAS_NEXT, String.valueOf(result.hasNext()));
        return headers;
    }
}
