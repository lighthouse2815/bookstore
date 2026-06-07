package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.RoleJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements IUserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public List<User> findAllActive() {
        return userJpaRepository.findAllActive().stream()
                .map(userPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findAllIncludingDeleted() {
        return userJpaRepository.findAllIncludingDeleted().stream()
                .map(userPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findByIdActive(UUID userId) {
        return userJpaRepository.findByIdActive(userId)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByIdIncludingDeleted(UUID userId) {
        return userJpaRepository.findByIdIncludingDeleted(userId)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameActive(String username) {
        return userJpaRepository.findByUsernameActive(username)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameIncludingDeleted(String username) {
        return userJpaRepository.findByUsernameIncludingDeleted(username)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID userId) {
        return userJpaRepository.existsByIdIncludingDeleted(userId);
    }

    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return userJpaRepository.existsByUsernameIncludingDeleted(username);
    }

    @Override
    public boolean existsByPhoneNumberIncludingDeleted(String phoneNumber) {
        return userJpaRepository.existsByPhoneNumberIncludingDeleted(phoneNumber);
    }

    @Override
    public boolean existsByEmailIncludingDeleted(String email) {
        return userJpaRepository.existsByEmailIncludingDeleted(email);
    }

    @Override
    public void deleteById(UUID userId) {
        userJpaRepository.deleteById(userId);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userJpaRepository.findByIdIncludingDeleted(user.getId())
                .orElseGet(UserJpaEntity::new);
        userPersistenceMapper.copyToEntity(user, entity, resolveRoles(user.getRoles()));
        return userPersistenceMapper.toDomain(userJpaRepository.save(entity));
    }

    private Set<RoleJpaEntity> resolveRoles(Set<Role> roles) {
        Set<RoleJpaEntity> resolved = new LinkedHashSet<>();
        for (Role role : roles) {
            RoleJpaEntity entity = roleJpaRepository.findByIdIncludingDeleted(role.getId())
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + role.getId()));
            resolved.add(entity);
        }
        return resolved;
    }
}
