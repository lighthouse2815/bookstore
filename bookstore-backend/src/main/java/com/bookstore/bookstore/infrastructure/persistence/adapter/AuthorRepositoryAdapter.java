package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.AuthorPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuthorJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthorRepositoryAdapter implements IAuthorRepository {

    private final AuthorJpaRepository authorJpaRepository;
    private final AuthorPersistenceMapper authorPersistenceMapper;

    @Override
    public List<Author> findAllActive() {
        return authorJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(authorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Author> findAllIncludingDeleted() {
        return authorJpaRepository.findAll().stream()
                .map(authorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Author> findByIdActive(UUID authorId) {
        return authorJpaRepository.findByIdAndDeletedAtIsNull(authorId)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Author> findByIdIncludingDeleted(UUID authorId) {
        return authorJpaRepository.findById(authorId)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Author> findByNameActive(String authorName) {
        return authorJpaRepository.findByNameAndDeletedAtIsNull(authorName)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID authorId) {
        return authorJpaRepository.existsById(authorId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String authorName) {
        return authorJpaRepository.existsByName(authorName);
    }

    @Override
    public Author save(Author author) {
        AuthorJpaEntity entity = authorJpaRepository.findById(author.getId())
                .orElseGet(AuthorJpaEntity::new);
        authorPersistenceMapper.copyToEntity(entity, author);
        return authorPersistenceMapper.toDomain(authorJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID authorId) {
        authorJpaRepository.deleteById(authorId);
    }
}
