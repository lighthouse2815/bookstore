package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.Order;
import java.util.Optional;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.DigitalAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private DigitalAssetJpaRepository digitalAssetJpaRepository;

    @Mock
    private CouponJpaRepository couponJpaRepository;

    @Mock
    private OrderPersistenceMapper orderPersistenceMapper;

    @InjectMocks
    private OrderRepositoryAdapter orderRepositoryAdapter;

    @Test
    void findPageByUserId_preservesIdPageOrderAfterGraphFetch() {
        UUID userId = UUID.randomUUID();
        UUID newestOrderId = UUID.randomUUID();
        UUID olderOrderId = UUID.randomUUID();
        OrderJpaEntity newestOrderEntity = orderEntity(newestOrderId);
        OrderJpaEntity olderOrderEntity = orderEntity(olderOrderId);
        Order newestOrder = org.mockito.Mockito.mock(Order.class);
        Order olderOrder = org.mockito.Mockito.mock(Order.class);

        when(orderJpaRepository.findPageIdsByUserIdAndUser_DeletedAtIsNullOrderByCreatedAtDesc(userId, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(newestOrderId, olderOrderId), PageRequest.of(0, 2), 4));
        when(orderJpaRepository.findAllByIdInAndUser_DeletedAtIsNull(List.of(newestOrderId, olderOrderId)))
                .thenReturn(List.of(olderOrderEntity, newestOrderEntity));
        when(orderPersistenceMapper.toDomain(newestOrderEntity)).thenReturn(newestOrder);
        when(orderPersistenceMapper.toDomain(olderOrderEntity)).thenReturn(olderOrder);

        var result = orderRepositoryAdapter.findPageByUserId(userId, 0, 2);

        assertEquals(List.of(newestOrder, olderOrder), result.items());
        assertEquals(4, result.totalCount());
        verify(orderJpaRepository).findPageIdsByUserIdAndUser_DeletedAtIsNullOrderByCreatedAtDesc(userId, PageRequest.of(0, 2));
        verify(orderJpaRepository).findAllByIdInAndUser_DeletedAtIsNull(List.of(newestOrderId, olderOrderId));
    }

    @Test
    void findPageAll_preservesIdPageOrderAfterGraphFetch() {
        UUID newestOrderId = UUID.randomUUID();
        UUID olderOrderId = UUID.randomUUID();
        OrderJpaEntity newestOrderEntity = orderEntity(newestOrderId);
        OrderJpaEntity olderOrderEntity = orderEntity(olderOrderId);
        Order newestOrder = org.mockito.Mockito.mock(Order.class);
        Order olderOrder = org.mockito.Mockito.mock(Order.class);

        when(orderJpaRepository.findPageIdsByUser_DeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(newestOrderId, olderOrderId), PageRequest.of(1, 2), 6));
        when(orderJpaRepository.findAllByIdInAndUser_DeletedAtIsNull(List.of(newestOrderId, olderOrderId)))
                .thenReturn(List.of(olderOrderEntity, newestOrderEntity));
        when(orderPersistenceMapper.toDomain(newestOrderEntity)).thenReturn(newestOrder);
        when(orderPersistenceMapper.toDomain(olderOrderEntity)).thenReturn(olderOrder);

        var result = orderRepositoryAdapter.findPageAll(1, 2);

        assertEquals(List.of(newestOrder, olderOrder), result.items());
        assertEquals(6, result.totalCount());
        verify(orderJpaRepository).findPageIdsByUser_DeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(1, 2));
        verify(orderJpaRepository).findAllByIdInAndUser_DeletedAtIsNull(List.of(newestOrderId, olderOrderId));
    }

    @Test
    void findByIdForUpdate_locksRowBeforeFetchingGraph() {
        UUID orderId = UUID.randomUUID();
        OrderJpaEntity lockedOrderEntity = orderEntity(orderId);
        OrderJpaEntity detailedOrderEntity = orderEntity(orderId);
        Order mappedOrder = org.mockito.Mockito.mock(Order.class);

        when(orderJpaRepository.findByIdAndUser_DeletedAtIsNullForUpdate(orderId))
                .thenReturn(Optional.of(lockedOrderEntity));
        when(orderJpaRepository.findByIdAndUser_DeletedAtIsNull(orderId))
                .thenReturn(Optional.of(detailedOrderEntity));
        when(orderPersistenceMapper.toDomain(detailedOrderEntity)).thenReturn(mappedOrder);

        var result = orderRepositoryAdapter.findByIdForUpdate(orderId);

        assertEquals(Optional.of(mappedOrder), result);
        verify(orderJpaRepository).findByIdAndUser_DeletedAtIsNullForUpdate(orderId);
        verify(orderJpaRepository).findByIdAndUser_DeletedAtIsNull(orderId);
    }

    private static OrderJpaEntity orderEntity(UUID orderId) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(orderId);
        return entity;
    }
}
