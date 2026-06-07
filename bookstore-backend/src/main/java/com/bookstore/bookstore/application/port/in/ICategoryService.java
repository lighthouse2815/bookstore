package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.DeleteCategoryCommand;
import com.bookstore.bookstore.application.command.UpdateCategoryCommand;
import com.bookstore.bookstore.domain.model.Category;
import java.util.List;
import java.util.UUID;

public interface ICategoryService {

    List<Category> getAll();

    List<Category> getAllIncludingDeleted();

    Category getById(UUID categoryId);

    Category getByIdIncludingDeleted(UUID categoryId);

    Category create(CreateCategoryCommand command);

    Category update(UpdateCategoryCommand command);

    void delete(DeleteCategoryCommand command);
}
