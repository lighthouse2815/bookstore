package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.DeleteCategoryCommand;
import com.bookstore.bookstore.application.command.UpdateCategoryCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ICategoryService;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.CategoryTranslation;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bookstore.bookstore.application.command.CategoryTranslationCommand;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private static final String CATEGORY_CODE_PATTERN = "[A-Z0-9_]+";

    private final ICategoryRepository categoryRepository;
    private final FileAssetPolicyService fileAssetPolicyService;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Category> getAll(PageQuery pageQuery) {
        int page = pageQuery.page();
        int size = pageQuery.size();
        return categoryRepository.findPageActive(page, size);
    }

    @Override
    public List<Category> getAllIncludingDeleted() {
        return categoryRepository.findAllIncludingDeleted();
    }

    @Override
    public Category getById(UUID categoryId) {
        if (categoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "categoryId");
        }

        return categoryRepository.findByIdActive(categoryId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public Category getByIdIncludingDeleted(UUID categoryId) {
        if (categoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "categoryId");
        }

        return categoryRepository.findByIdIncludingDeleted(categoryId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category create(CreateCategoryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String code = normalizeCode(command.code());
        Map<CategoryLocale, CategoryTranslation> translations = normalizeTranslations(command.translations());
        CategoryTranslation vietnamese = translations.get(CategoryLocale.VI);
        String name = vietnamese.name();
        String description = vietnamese.description();
        UUID parentId = command.parentId();
        FileAsset imageFileAsset = resolveImageFileAsset(command.imageFileAssetId());

        if (categoryRepository.existsByCodeIncludingDeleted(code)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_CODE_ALREADY_EXISTS);
        }

        if (categoryRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        requireActiveParentCategory(parentId, null);

        Instant now = Instant.now();
        Category category = new Category(
                UUID.randomUUID(),
                code,
                name,
                description,
                translations,
                parentId,
                imageFileAsset,
                now,
                now,
                null
        );

        return categoryRepository.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category update(UpdateCategoryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Category currentCategory = categoryRepository.findByIdActive(command.categoryId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));

        String code = normalizeCode(command.code());
        Map<CategoryLocale, CategoryTranslation> translations = normalizeTranslations(command.translations());
        CategoryTranslation vietnamese = translations.get(CategoryLocale.VI);
        String name = vietnamese.name();
        String description = vietnamese.description();
        UUID parentId = command.parentId();
        FileAsset imageFileAsset = resolveImageFileAsset(command.imageFileAssetId());

        if (!currentCategory.getCode().equals(code) && categoryRepository.existsByCodeIncludingDeleted(code)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_CODE_ALREADY_EXISTS);
        }

        if (!currentCategory.getName().equals(name) && categoryRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        requireActiveParentCategory(parentId, currentCategory.getId());
        currentCategory.updateCategory(code, name, description, translations, parentId, imageFileAsset);
        return categoryRepository.save(currentCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteCategoryCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Category currentCategory = categoryRepository.findByIdActive(command.categoryId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));

        currentCategory.softDelete();
        categoryRepository.save(currentCategory);
    }

    private void requireActiveParentCategory(UUID parentId, UUID categoryId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(parentId, categoryId)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "parentId");
        }
        if (categoryRepository.findByIdActive(parentId).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    private FileAsset resolveImageFileAsset(UUID imageFileAssetId) {
        if (imageFileAssetId == null) {
            return null;
        }

        return fileAssetPolicyService.requireActiveAsset(
                imageFileAssetId,
                FilePurpose.CATEGORY_IMAGE,
                FileVisibility.PUBLIC
        );
    }

    private String normalizeCode(String rawCode) {
        String code = StringUtils.trimToNull(rawCode);
        if (code == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "code");
        }
        code = code.toUpperCase(Locale.ROOT);
        if (!code.matches(CATEGORY_CODE_PATTERN)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "code");
        }
        return code;
    }

    private Map<CategoryLocale, CategoryTranslation> normalizeTranslations(
            List<CategoryTranslationCommand> commands
    ) {
        if (commands == null || commands.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "translations");
        }

        Map<CategoryLocale, CategoryTranslation> translations = new LinkedHashMap<>();
        commands.forEach(command -> {
            if (command == null) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "translations");
            }
            CategoryLocale locale = command.locale();
            if (translations.containsKey(locale)) {
                throw new ApplicationException(
                        ApplicationErrorCode.INVALID_ARGUMENT,
                        "translations." + locale.getCode()
                );
            }
            String name = StringUtils.trimToNull(command.name());
            if (name == null) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
            }
            translations.put(locale, new CategoryTranslation(
                    locale,
                    name,
                    StringUtils.trimToNull(command.description())
            ));
        });

        if (!translations.containsKey(CategoryLocale.VI)
                || !translations.containsKey(CategoryLocale.EN)) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "translations.vi,en");
        }
        return Map.copyOf(translations);
    }

}
