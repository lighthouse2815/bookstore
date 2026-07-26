package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfilePersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    public Profile toDomain(ProfileJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Profile(
                entity.getId(),
                entity.getUser().getId(),
                entity.getLastName(),
                entity.getFirstName(),
                fileAssetPersistenceMapper.toDomain(entity.getAvatarFileAsset()),
                entity.getGender(),
                entity.getDateOfBirth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public ProfileJpaEntity toEntity(Profile profile, UserJpaEntity user) {
        ProfileJpaEntity entity = new ProfileJpaEntity();
        copyToEntity(profile, entity, user, null);
        return entity;
    }

    public void copyToEntity(
            Profile profile,
            ProfileJpaEntity entity,
            UserJpaEntity user,
            FileAssetJpaEntity avatarFileAsset
    ) {
        entity.setId(profile.getId());
        entity.setUser(user);
        entity.setLastName(profile.getLastName());
        entity.setFirstName(profile.getFirstName());
        entity.setAvatarFileAsset(avatarFileAsset);
        entity.setAvatarUrl(null);
        entity.setGender(profile.getGender());
        entity.setDateOfBirth(profile.getDateOfBirth());
        entity.setCreatedAt(profile.getCreatedAt());
        entity.setUpdatedAt(profile.getUpdatedAt());
        entity.setDeletedAt(profile.getDeletedAt());
    }
}
