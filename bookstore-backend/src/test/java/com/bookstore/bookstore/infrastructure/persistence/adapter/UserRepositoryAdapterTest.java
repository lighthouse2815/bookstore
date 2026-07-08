package com.bookstore.bookstore.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.RoleJpaRepository;
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
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @Mock
    private UserPersistenceMapper userPersistenceMapper;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void findPageByRoleNameActive_preservesIdPageOrderAfterGraphFetch() {
        String roleName = "ADMIN";
        UUID newestUserId = UUID.randomUUID();
        UUID olderUserId = UUID.randomUUID();
        UserJpaEntity newestUserEntity = userEntity(newestUserId);
        UserJpaEntity olderUserEntity = userEntity(olderUserId);
        User newestUser = org.mockito.Mockito.mock(User.class);
        User olderUser = org.mockito.Mockito.mock(User.class);

        when(userJpaRepository.findPageIdsByRoleNameActive(roleName, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(newestUserId, olderUserId), PageRequest.of(0, 2), 3));
        when(userJpaRepository.findAllByIdInAndDeletedAtIsNull(List.of(newestUserId, olderUserId)))
                .thenReturn(List.of(olderUserEntity, newestUserEntity));
        when(userPersistenceMapper.toDomain(newestUserEntity)).thenReturn(newestUser);
        when(userPersistenceMapper.toDomain(olderUserEntity)).thenReturn(olderUser);

        var result = userRepositoryAdapter.findPageByRoleNameActive(roleName, 0, 2);

        assertEquals(List.of(newestUser, olderUser), result.items());
        assertEquals(3, result.totalCount());
        verify(userJpaRepository).findPageIdsByRoleNameActive(roleName, PageRequest.of(0, 2));
        verify(userJpaRepository).findAllByIdInAndDeletedAtIsNull(List.of(newestUserId, olderUserId));
    }

    private static UserJpaEntity userEntity(UUID userId) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(userId);
        return entity;
    }
}
