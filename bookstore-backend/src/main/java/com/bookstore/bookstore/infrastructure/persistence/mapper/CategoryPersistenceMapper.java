package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.CategoryTranslation;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryTranslationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                toDomainTranslations(entity),
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
        entity.setCode(category.getCode());
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        copyTranslations(entity, category.getTranslations());
        entity.setParentId(category.getParentId());
        entity.setImageFileAsset(imageFileAsset);
        entity.setCreatedAt(category.getCreatedAt());
        entity.setUpdatedAt(category.getUpdatedAt());
        entity.setDeletedAt(category.getDeletedAt());
    }

    public void copyToEntity(CategoryJpaEntity entity, Category category) {
        copyToEntity(entity, category, null);
    }

    private Map<CategoryLocale, CategoryTranslation> toDomainTranslations(CategoryJpaEntity entity) {
        Map<CategoryLocale, CategoryTranslation> result = new LinkedHashMap<>();
        entity.getTranslations().forEach(translation -> {
            CategoryLocale locale = CategoryLocale.fromCode(translation.getLocale());
            result.put(
                    locale,
                    new CategoryTranslation(
                            locale,
                            translation.getName(),
                            translation.getDescription()
                    )
            );
        });
        return result;
    }

    private void copyTranslations(
            CategoryJpaEntity entity,
            Map<CategoryLocale, CategoryTranslation> translations
    ) {
        Map<CategoryLocale, CategoryTranslationJpaEntity> currentByLocale = new LinkedHashMap<>();
        entity.getTranslations().forEach(translation -> currentByLocale.put(
                CategoryLocale.fromCode(translation.getLocale()),
                translation
        ));
        entity.getTranslations().removeIf(translation ->
                !translations.containsKey(CategoryLocale.fromCode(translation.getLocale()))
        );

        translations.forEach((locale, translation) -> {
            CategoryTranslationJpaEntity target = currentByLocale.get(locale);
            if (target == null) {
                target = new CategoryTranslationJpaEntity();
                target.setId(UUID.randomUUID());
                target.setCategory(entity);
                target.setLocale(locale.getCode());
                entity.getTranslations().add(target);
            }
            target.setName(translation.name());
            target.setDescription(translation.description());
        });
    }
}
