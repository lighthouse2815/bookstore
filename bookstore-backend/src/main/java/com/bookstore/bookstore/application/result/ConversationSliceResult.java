package com.bookstore.bookstore.application.result;

import java.util.List;

public record ConversationSliceResult(
        List<ConversationResult> items,
        long totalCount,
        int page,
        int size
) {
    public boolean hasNext() {
        return (long) (page + 1) * size < totalCount;
    }
}
