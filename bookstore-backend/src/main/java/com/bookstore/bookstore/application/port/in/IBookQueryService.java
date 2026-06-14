package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.BookPageDetailResult;
import com.bookstore.bookstore.application.result.BookQueryResult;
import java.util.List;
import java.util.UUID;

public interface IBookQueryService {

    List<BookQueryResult> getAll();

    List<BookQueryResult> search(String keyword);

    BookQueryResult getById(UUID bookId);

    BookPageDetailResult getPageDetail(UUID bookId, int relatedLimit);

    List<BookQueryResult> getRelatedBooks(UUID bookId, int limit);
}
