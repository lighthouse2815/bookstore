package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CategoryJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements ICategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryPersistenceMapper categoryPersistenceMapper;

    @Override
    public List<Category> findAllActive() {
        return categoryJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(categoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllIncludingDeleted() {
        return categoryJpaRepository.findAll().stream()
                .map(categoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findByIdActive(UUID categoryId) {
        return categoryJpaRepository.findByIdAndDeletedAtIsNull(categoryId)
                .map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Category> findByIdIncludingDeleted(UUID categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Category> findByNameActive(String categoryName) {
        return categoryJpaRepository.findByNameAndDeletedAtIsNull(categoryName)
                .map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID categoryId) {
        return categoryJpaRepository.existsById(categoryId);
    }


    @Override
    public boolean existsByNameIncludingDeleted(String categoryName) {
        return categoryJpaRepository.existsByName(categoryName);
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = categoryJpaRepository.findById(category.getId())
                .orElseGet(CategoryJpaEntity::new);

        categoryPersistenceMapper.copyToEntity(entity, category);
        return categoryPersistenceMapper.toDomain(categoryJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID categoryId) {
        categoryJpaRepository.deleteById(categoryId);
    }
}
