package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.WishlistItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IWishlistRepository {

    List<WishlistItem> findAllByUserIdActive(UUID userId);

    Optional<WishlistItem> findByUserIdAndBookId(UUID userId, UUID bookId);

    WishlistItem save(WishlistItem wishlistItem);
}
