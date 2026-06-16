package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserAuthIdentityJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAuthIdentityPersistenceMapper {

    public UserAuthIdentity toDomain(UserAuthIdentityJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserAuthIdentity(
                entity.getId(),
                entity.getUser().getId(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getProviderEmail(),
                entity.isEmailVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(UserAuthIdentity userAuthIdentity, UserAuthIdentityJpaEntity entity, UserJpaEntity user) {
        entity.setId(userAuthIdentity.getId());
        entity.setUser(user);
        entity.setProvider(userAuthIdentity.getProvider());
        entity.setProviderSubject(userAuthIdentity.getProviderSubject());
        entity.setProviderEmail(userAuthIdentity.getProviderEmail());
        entity.setEmailVerified(userAuthIdentity.isEmailVerified());
        entity.setCreatedAt(userAuthIdentity.getCreatedAt());
        entity.setUpdatedAt(userAuthIdentity.getUpdatedAt());
    }
}
