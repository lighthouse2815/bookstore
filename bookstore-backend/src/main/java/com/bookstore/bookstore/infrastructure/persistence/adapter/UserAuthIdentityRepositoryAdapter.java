package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IUserAuthIdentityRepository;
import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserAuthIdentityJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserAuthIdentityPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserAuthIdentityJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAuthIdentityRepositoryAdapter implements IUserAuthIdentityRepository {

    private final UserAuthIdentityJpaRepository userAuthIdentityJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserAuthIdentityPersistenceMapper userAuthIdentityPersistenceMapper;

    @Override
    public Optional<UserAuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject) {
        return userAuthIdentityJpaRepository.findByProviderAndProviderSubject(provider, providerSubject)
                .map(userAuthIdentityPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserAuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider) {
        return userAuthIdentityJpaRepository.findByUserIdAndProvider(userId, provider)
                .map(userAuthIdentityPersistenceMapper::toDomain);
    }

    @Override
    public UserAuthIdentity save(UserAuthIdentity userAuthIdentity) {
        UserAuthIdentityJpaEntity entity = userAuthIdentityJpaRepository.findById(userAuthIdentity.getId())
                .orElseGet(UserAuthIdentityJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(userAuthIdentity.getUserId());
        userAuthIdentityPersistenceMapper.copyToEntity(userAuthIdentity, entity, user);
        return userAuthIdentityPersistenceMapper.toDomain(userAuthIdentityJpaRepository.save(entity));
    }
}
