package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CartPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CartJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements ICartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartPersistenceMapper cartPersistenceMapper;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartJpaRepository.findByUser_Id(userId)
                .map(cartPersistenceMapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity entity = cartJpaRepository.findById(cart.getId())
                .orElseGet(CartJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(cart.getUserId());
        
        Map<UUID, BookJpaEntity> bookMap = cart.getItems().stream()
                .map(item -> item.getBookId())
                .distinct()
                .collect(Collectors.toMap(
                    bookId -> bookId,
                    bookJpaRepository::getReferenceById
                ));
        
        cartPersistenceMapper.copyToEntityWithBooks(cart, entity, user, bookMap);
        return cartPersistenceMapper.toDomain(cartJpaRepository.save(entity));
    }
}
