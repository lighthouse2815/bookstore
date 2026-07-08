package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.BookQueryResult;
import java.util.List;
import java.util.UUID;

public interface IWishlistService {

    List<BookQueryResult> getMyWishlist(UUID userId);

    void addBook(UUID userId, UUID bookId);

    void removeBook(UUID userId, UUID bookId);
}
