package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfilePersistenceMapper {

    public Profile toDomain(ProfileJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Profile(
                entity.getId(),
                entity.getUser().getId(),
                entity.getLastName(),
                entity.getFirstName(),
                entity.getAvatarUrl(),
                entity.getGender(),
                entity.getDateOfBirth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public ProfileJpaEntity toEntity(Profile profile, UserJpaEntity user) {
        ProfileJpaEntity entity = new ProfileJpaEntity();
        copyToEntity(profile, entity, user);
        return entity;
    }

    public void copyToEntity(Profile profile, ProfileJpaEntity entity, UserJpaEntity user) {
        entity.setId(profile.getId());
        entity.setUser(user);
        entity.setLastName(profile.getLastName());
        entity.setFirstName(profile.getFirstName());
        entity.setAvatarUrl(profile.getAvatarUrl());
        entity.setGender(profile.getGender());
        entity.setDateOfBirth(profile.getDateOfBirth());
        entity.setCreatedAt(profile.getCreatedAt());
        entity.setUpdatedAt(profile.getUpdatedAt());
        entity.setDeletedAt(profile.getDeletedAt());
    }
}
