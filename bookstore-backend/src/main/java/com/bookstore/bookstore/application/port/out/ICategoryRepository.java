package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository {

    List<Category> findAllActive();

    List<Category> findAllIncludingDeleted();

    Optional<Category> findByIdActive(UUID categoryId);

    Optional<Category> findByIdIncludingDeleted(UUID categoryId);

    Optional<Category> findByNameActive(String categoryName);

    boolean existsByIdIncludingDeleted(UUID categoryId);

    boolean existsByNameIncludingDeleted(String categoryName);

    Category save(Category category);

    void deleteById(UUID categoryId);
}
