package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IWishlistRepository;
import com.bookstore.bookstore.domain.model.WishlistItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.WishlistItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.WishlistItemPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.WishlistItemJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WishlistRepositoryAdapter implements IWishlistRepository {

    private final WishlistItemJpaRepository wishlistItemJpaRepository;
    private final WishlistItemPersistenceMapper wishlistItemPersistenceMapper;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public List<WishlistItem> findAllByUserIdActive(UUID userId) {
        return wishlistItemJpaRepository.findAllByUserIdActive(userId).stream()
                .map(wishlistItemPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<WishlistItem> findByUserIdAndBookId(UUID userId, UUID bookId) {
        return wishlistItemJpaRepository.findByUserIdAndBookId(userId, bookId)
                .map(wishlistItemPersistenceMapper::toDomain);
    }

    @Override
    public WishlistItem save(WishlistItem wishlistItem) {
        WishlistItemJpaEntity entity = wishlistItemJpaRepository.findById(wishlistItem.getId())
                .orElseGet(WishlistItemJpaEntity::new);
        wishlistItemPersistenceMapper.copyToEntity(
                wishlistItem,
                entity,
                userJpaRepository.getReferenceById(wishlistItem.getUserId()),
                bookJpaRepository.getReferenceById(wishlistItem.getBookId())
        );
        return wishlistItemPersistenceMapper.toDomain(wishlistItemJpaRepository.save(entity));
    }
}
