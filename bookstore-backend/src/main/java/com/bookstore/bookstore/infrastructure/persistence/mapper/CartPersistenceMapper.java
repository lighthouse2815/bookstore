package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
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

    public void copyToEntityWithReferences(
            Cart cart,
            CartJpaEntity entity,
            UserJpaEntity user,
            Map<UUID, BookJpaEntity> bookMap,
            Map<UUID, DigitalAssetJpaEntity> digitalAssetMap
    ) {
        entity.setId(cart.getId());
        entity.setUser(user);
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());

        Map<UUID, CartItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(CartItemJpaEntity::getId, Function.identity()));

        var mappedItems = cart.getItems().stream()
                .map(item -> {
                    CartItemJpaEntity itemEntity = currentItems.getOrDefault(item.getId(), new CartItemJpaEntity());
                    copyItemToEntity(
                            item,
                            itemEntity,
                            entity,
                            item.getBookId() == null ? null : bookMap.get(item.getBookId()),
                            item.getDigitalAssetId() == null ? null : digitalAssetMap.get(item.getDigitalAssetId())
                    );
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private CartItem toDomain(CartItemJpaEntity entity) {
        return new CartItem(
                entity.getId(),
                entity.getItemType() == null ? PurchaseItemType.PHYSICAL_BOOK : entity.getItemType(),
                entity.getBook() == null ? null : entity.getBook().getId(),
                entity.getDigitalAsset() == null ? null : entity.getDigitalAsset().getId(),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void copyItemToEntity(
            CartItem item,
            CartItemJpaEntity entity,
            CartJpaEntity cartEntity,
            BookJpaEntity book,
            DigitalAssetJpaEntity digitalAsset
    ) {
        entity.setId(item.getId());
        entity.setCart(cartEntity);
        entity.setItemType(item.getItemType());
        entity.setBook(book);
        entity.setDigitalAsset(digitalAsset);
        entity.setQuantity(item.getQuantity());
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
    }
}
