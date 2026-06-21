package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;

public record DigitalLibraryAssetResult(
        UserDigitalAccess access,
        DigitalAsset asset,
        Book book,
        ReadingProgress progress
) {
}
