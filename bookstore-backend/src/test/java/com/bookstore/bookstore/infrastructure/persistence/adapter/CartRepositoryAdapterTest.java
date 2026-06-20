package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CartPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CartJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartRepositoryAdapterTest {

    @Mock
    private CartJpaRepository cartJpaRepository;

    @Mock
    private CartPersistenceMapper cartPersistenceMapper;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @InjectMocks
    private CartRepositoryAdapter cartRepositoryAdapter;

    @Test
    void findByUserId_looksUpCartByUserForeignKey() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.EPOCH;
        CartJpaEntity entity = new CartJpaEntity();
        Cart expected = new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );

        when(cartJpaRepository.findByUser_Id(userId)).thenReturn(Optional.of(entity));
        when(cartPersistenceMapper.toDomain(entity)).thenReturn(expected);

        Optional<Cart> result = cartRepositoryAdapter.findByUserId(userId);

        assertTrue(result.isPresent());
        assertSame(expected, result.orElseThrow());
        verify(cartJpaRepository).findByUser_Id(userId);
        verify(cartJpaRepository, never()).findById(userId);
    }
}
