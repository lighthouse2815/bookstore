package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CartPersistenceMapper {

    public Cart toDomain(CartJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Cart(
                entity.getId(),
                entity.getUser().getId(),
                entity.getItems().stream()
                        .map(this::toDomain)
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(Cart cart, CartJpaEntity entity, UserJpaEntity user) {
        entity.setId(cart.getId());
        entity.setUser(user);
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());

        Map<UUID, CartItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(CartItemJpaEntity::getId, Function.identity()));

        var mappedItems = cart.getItems().stream()
                .map(item -> {
                    CartItemJpaEntity itemEntity = currentItems.getOrDefault(item.getId(), new CartItemJpaEntity());
                    // copyItemToEntity will be called by adapter with book reference
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    public void copyToEntityWithBooks(Cart cart, CartJpaEntity entity, UserJpaEntity user, Map<UUID, BookJpaEntity> bookMap) {
        entity.setId(cart.getId());
        entity.setUser(user);
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());

        Map<UUID, CartItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(CartItemJpaEntity::getId, Function.identity()));

        var mappedItems = cart.getItems().stream()
                .map(item -> {
                    CartItemJpaEntity itemEntity = currentItems.getOrDefault(item.getId(), new CartItemJpaEntity());
                    BookJpaEntity book = bookMap.get(item.getBookId());
                    copyItemToEntity(item, itemEntity, entity, book);
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private CartItem toDomain(CartItemJpaEntity entity) {
        return new CartItem(
                entity.getId(),
                entity.getBook().getId(),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void copyItemToEntity(CartItem item, CartItemJpaEntity entity, CartJpaEntity cartEntity, BookJpaEntity book) {
        entity.setId(item.getId());
        entity.setCart(cartEntity);
        entity.setBook(book);
        entity.setQuantity(item.getQuantity());
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
    }
}
