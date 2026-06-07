package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.DeleteCategoryCommand;
import com.bookstore.bookstore.application.command.UpdateCategoryCommand;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.presentation.request.CreateCategoryRequest;
import com.bookstore.bookstore.presentation.request.UpdateCategoryRequest;
import com.bookstore.bookstore.presentation.response.CategoryResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryWebMapper {

    public CreateCategoryCommand toCreateCommand(CreateCategoryRequest request) {
        return new CreateCategoryCommand(
                request.name(),
                request.description()
        );
    }

    public UpdateCategoryCommand toUpdateCommand(UUID categoryId, UpdateCategoryRequest request) {
        return new UpdateCategoryCommand(
                categoryId,
                request.name(),
                request.description()
        );
    }

    public DeleteCategoryCommand toDeleteCommand(UUID categoryId) {
        return new DeleteCategoryCommand(categoryId);
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
