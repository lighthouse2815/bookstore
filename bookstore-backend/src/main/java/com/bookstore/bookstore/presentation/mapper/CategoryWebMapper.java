package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.CategoryTranslationCommand;
import com.bookstore.bookstore.application.command.DeleteCategoryCommand;
import com.bookstore.bookstore.application.command.UpdateCategoryCommand;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.presentation.request.CreateCategoryRequest;
import com.bookstore.bookstore.presentation.request.UpdateCategoryRequest;
import com.bookstore.bookstore.presentation.response.CategoryResponse;
import com.bookstore.bookstore.presentation.response.CategoryTranslationResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryWebMapper {

    public CreateCategoryCommand toCreateCommand(CreateCategoryRequest request) {
        return new CreateCategoryCommand(
                request.code(),
                request.translations().stream()
                        .map(item -> new CategoryTranslationCommand(item.locale(), item.name(), item.description()))
                        .toList(),
                request.parentId(),
                request.imageFileAssetId()
        );
    }

    public UpdateCategoryCommand toUpdateCommand(UUID categoryId, UpdateCategoryRequest request) {
        return new UpdateCategoryCommand(
                categoryId,
                request.code(),
                request.translations().stream()
                        .map(item -> new CategoryTranslationCommand(item.locale(), item.name(), item.description()))
                        .toList(),
                request.parentId(),
                request.imageFileAssetId()
        );
    }

    public DeleteCategoryCommand toDeleteCommand(UUID categoryId) {
        return new DeleteCategoryCommand(categoryId);
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                toTranslationResponses(category),
                category.getParentId(),
                category.getImageFileAssetId(),
                category.getImageUrl(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public Map<String, CategoryTranslationResponse> toTranslationResponses(Category category) {
        Map<String, CategoryTranslationResponse> result = new LinkedHashMap<>();
        category.getTranslations().forEach((locale, translation) -> result.put(
                locale,
                new CategoryTranslationResponse(locale, translation.name(), translation.description())
        ));
        return result;
    }
}
