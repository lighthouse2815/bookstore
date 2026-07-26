package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;

public record PublicDigitalAssetCatalogItemResult(
        DigitalAsset asset,
        Book book
) {
}
