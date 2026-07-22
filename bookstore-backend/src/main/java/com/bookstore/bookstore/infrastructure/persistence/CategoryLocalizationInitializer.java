package com.bookstore.bookstore.infrastructure.persistence;

import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryTranslationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.repository.CategoryJpaRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(110)
@RequiredArgsConstructor
public class CategoryLocalizationInitializer implements ApplicationRunner {

    private final CategoryJpaRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, DevelopmentSeedCatalog.CategorySeed> definitions = new LinkedHashMap<>();
        DevelopmentSeedCatalog.CATEGORIES.forEach(definition -> definitions.put(definition.name(), definition));

        for (CategoryJpaEntity category : categoryRepository.findAll()) {
            DevelopmentSeedCatalog.CategorySeed definition = definitions.get(category.getName());
            boolean changed = false;

            if (category.getCode() == null || category.getCode().isBlank()) {
                category.setCode(definition == null ? fallbackCode(category.getId()) : definition.code());
                changed = true;
            }

            Map<String, CategoryTranslationJpaEntity> translations = new LinkedHashMap<>();
            category.getTranslations().forEach(translation -> translations.put(translation.getLocale(), translation));

            if (!translations.containsKey("vi")) {
                addTranslation(category, "vi", category.getName(), category.getDescription());
                changed = true;
            }
            if (!translations.containsKey("en")) {
                addTranslation(
                        category,
                        "en",
                        definition == null ? category.getName() : definition.englishName(),
                        definition == null ? category.getDescription() : definition.englishDescription()
                );
                changed = true;
            }

            if (changed) {
                categoryRepository.save(category);
            }
        }
    }

    private void addTranslation(
            CategoryJpaEntity category,
            String locale,
            String name,
            String description
    ) {
        CategoryTranslationJpaEntity translation = new CategoryTranslationJpaEntity();
        translation.setId(UUID.randomUUID());
        translation.setCategory(category);
        translation.setLocale(locale);
        translation.setName(name);
        translation.setDescription(description);
        category.getTranslations().add(translation);
    }

    private String fallbackCode(UUID categoryId) {
        return "CATEGORY_" + categoryId.toString().replace("-", "").toUpperCase();
    }
}
