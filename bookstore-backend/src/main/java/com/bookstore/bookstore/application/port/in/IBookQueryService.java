package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.BookPageDetailResult;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IBookQueryService {

    List<BookQueryResult> getAll();

    PageSliceResult<BookQueryResult> getAll(int page, int size);

    List<BookQueryResult> search(String keyword);

    PageSliceResult<BookQueryResult> search(String keyword, int page, int size);

    PageSliceResult<BookQueryResult> search(String keyword, UUID categoryId, int page, int size);

    BookQueryResult getById(UUID bookId);

    BookPageDetailResult getPageDetail(UUID bookId, int relatedLimit);

    List<BookQueryResult> getRelatedBooks(UUID bookId, int limit);
}
