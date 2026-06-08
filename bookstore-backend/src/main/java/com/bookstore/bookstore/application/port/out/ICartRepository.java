package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Cart;
import java.util.Optional;
import java.util.UUID;

public interface ICartRepository {

    Optional<Cart> findByUserId(UUID userId);

    Cart save(Cart cart);
}
