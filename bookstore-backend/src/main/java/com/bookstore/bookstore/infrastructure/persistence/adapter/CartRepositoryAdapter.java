package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CartPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CartJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements ICartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartPersistenceMapper cartPersistenceMapper;

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartJpaRepository.findByUserId(userId)
                .map(cartPersistenceMapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity entity = cartJpaRepository.findByUserId(cart.getUserId())
                .orElseGet(CartJpaEntity::new);
        cartPersistenceMapper.copyToEntity(cart, entity);
        return cartPersistenceMapper.toDomain(cartJpaRepository.save(entity));
    }
}
