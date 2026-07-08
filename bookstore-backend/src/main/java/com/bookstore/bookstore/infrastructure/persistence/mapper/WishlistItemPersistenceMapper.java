package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.WishlistItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.WishlistItemJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WishlistItemPersistenceMapper {

    public WishlistItem toDomain(WishlistItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new WishlistItem(
                entity.getId(),
                entity.getUser().getId(),
                entity.getBook().getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            WishlistItem wishlistItem,
            WishlistItemJpaEntity entity,
            UserJpaEntity user,
            BookJpaEntity book
    ) {
        entity.setId(wishlistItem.getId());
        entity.setUser(user);
        entity.setBook(book);
        entity.setCreatedAt(wishlistItem.getCreatedAt());
        entity.setUpdatedAt(wishlistItem.getUpdatedAt());
        entity.setDeletedAt(wishlistItem.getDeletedAt());
    }
}
