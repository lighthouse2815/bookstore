package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository {

    List<Category> findAllActive();

    PageSliceResult<Category> findPageActive(int page, int size);

    List<Category> findAllIncludingDeleted();

    Optional<Category> findByIdActive(UUID categoryId);

    Optional<Category> findByIdIncludingDeleted(UUID categoryId);

    Optional<Category> findByNameActive(String categoryName);

    boolean existsByIdIncludingDeleted(UUID categoryId);

    boolean existsByNameIncludingDeleted(String categoryName);

    Category save(Category category);

    void deleteById(UUID categoryId);
}
