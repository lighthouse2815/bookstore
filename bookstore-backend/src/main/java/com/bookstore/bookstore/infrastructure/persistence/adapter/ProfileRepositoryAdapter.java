package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ProfilePersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ProfileJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileRepositoryAdapter implements IProfileRepository {

    private final ProfileJpaRepository profileJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProfilePersistenceMapper profilePersistenceMapper;

    @Override
    public List<Profile> findAllActive() {
        return profileJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(profilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Profile> findAllIncludingDeleted() {
        return profileJpaRepository.findAll().stream()
                .map(profilePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Profile> findByIdActive(UUID profileId) {
        return profileJpaRepository.findByIdAndDeletedAtIsNull(profileId)
                .map(profilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByIdIncludingDeleted(UUID profileId) {
        return profileJpaRepository.findById(profileId)
                .map(profilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByUserIdActive(UUID userId) {
        return profileJpaRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(profilePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByUserIdIncludingDeleted(UUID userId) {
        return profileJpaRepository.findByUserId(userId)
                .map(profilePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID profileId) {
        return profileJpaRepository.existsById(profileId);
    }

    @Override
    public boolean existsByUserIdIncludingDeleted(UUID userId) {
        return profileJpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteById(UUID profileId) {
        profileJpaRepository.deleteById(profileId);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = profileJpaRepository.findById(profile.getId())
                .orElseGet(ProfileJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(profile.getUserId());
        profilePersistenceMapper.copyToEntity(profile, entity, user);
        return profilePersistenceMapper.toDomain(profileJpaRepository.save(entity));
    }
}
