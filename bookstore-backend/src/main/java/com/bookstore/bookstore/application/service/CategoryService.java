package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.DeleteCategoryCommand;
import com.bookstore.bookstore.application.command.UpdateCategoryCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ICategoryService;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final ICategoryRepository categoryRepository;
    private final FileAssetPolicyService fileAssetPolicyService;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Category> getAll(int page, int size) {
        validatePageRequest(page, size);
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

        String name = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());
        UUID parentId = command.parentId();
        FileAsset imageFileAsset = resolveImageFileAsset(command.imageFileAssetId());

        if (categoryRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        requireActiveParentCategory(parentId, null);

        Instant now = Instant.now();
        Category category = new Category(
                UUID.randomUUID(),
                name,
                description,
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

        String name = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());
        UUID parentId = command.parentId();
        FileAsset imageFileAsset = resolveImageFileAsset(command.imageFileAssetId());

        if (!currentCategory.getName().equals(name) && categoryRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        requireActiveParentCategory(parentId, currentCategory.getId());
        currentCategory.updateCategory(name, description, parentId, imageFileAsset);
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

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
    }
}
