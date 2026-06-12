package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserAuthIdentityJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthIdentityJpaRepository extends JpaRepository<UserAuthIdentityJpaEntity, UUID> {

    Optional<UserAuthIdentityJpaEntity> findByProviderAndProviderSubject(
            AuthProvider provider,
            String providerSubject
    );

    Optional<UserAuthIdentityJpaEntity> findByUserIdAndProvider(UUID userId, AuthProvider provider);
}
