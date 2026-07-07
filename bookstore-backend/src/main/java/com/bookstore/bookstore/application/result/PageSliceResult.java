package com.bookstore.bookstore.application.result;

import java.util.List;
import java.util.function.Function;

public record PageSliceResult<T>(
        List<T> items,
        long totalCount,
        int page,
        int size
) {

    public PageSliceResult {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalCount;
    }

    public <R> PageSliceResult<R> map(Function<? super T, R> mapper) {
        return new PageSliceResult<>(
                items.stream()
                        .map(mapper)
                        .toList(),
                totalCount,
                page,
                size
        );
    }
}
