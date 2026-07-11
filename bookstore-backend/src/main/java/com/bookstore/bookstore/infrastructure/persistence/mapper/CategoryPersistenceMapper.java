package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Category(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getParentId(),
                fileAssetPersistenceMapper.toDomain(entity.getImageFileAsset()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public CategoryJpaEntity toEntity(Category category) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        copyToEntity(entity, category);
        return entity;
    }

    public void copyToEntity(CategoryJpaEntity entity, Category category, FileAssetJpaEntity imageFileAsset) {
        entity.setId(category.getId());
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        entity.setParentId(category.getParentId());
        entity.setImageFileAsset(imageFileAsset);
        entity.setCreatedAt(category.getCreatedAt());
        entity.setUpdatedAt(category.getUpdatedAt());
        entity.setDeletedAt(category.getDeletedAt());
    }

    public void copyToEntity(CategoryJpaEntity entity, Category category) {
        copyToEntity(entity, category, null);
    }
}
