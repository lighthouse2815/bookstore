package com.bookstore.bookstore.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryTranslationJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryPersistenceMapperTest {

    private final CategoryPersistenceMapper categoryPersistenceMapper =
            new CategoryPersistenceMapper(new FileAssetPersistenceMapper());

    @Test
    void mapsLocaleBetweenDatabaseCodeAndDomainEnumWithoutReplacingExistingRow() {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode("LITERATURE");
        entity.setName("Văn học");
        entity.setDescription("Sách văn học");
        entity.setCreatedAt(Instant.EPOCH);
        entity.setUpdatedAt(Instant.EPOCH);

        CategoryTranslationJpaEntity translationEntity = new CategoryTranslationJpaEntity();
        translationEntity.setId(UUID.randomUUID());
        translationEntity.setCategory(entity);
        translationEntity.setLocale("vi");
        translationEntity.setName("Văn học");
        translationEntity.setDescription("Sách văn học");
        entity.getTranslations().add(translationEntity);

        var category = categoryPersistenceMapper.toDomain(entity);

        assertTrue(category.getTranslations().containsKey(CategoryLocale.VI));
        assertEquals(
                CategoryLocale.VI,
                category.getTranslations().get(CategoryLocale.VI).locale()
        );

        categoryPersistenceMapper.copyToEntity(entity, category, null);

        CategoryTranslationJpaEntity persistedTranslation =
                entity.getTranslations().iterator().next();
        assertSame(translationEntity, persistedTranslation);
        assertEquals("vi", persistedTranslation.getLocale());
    }
}
