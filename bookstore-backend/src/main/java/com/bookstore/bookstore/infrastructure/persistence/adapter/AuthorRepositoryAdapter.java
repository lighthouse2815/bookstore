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
        return authorJpaRepository.findAllActive().stream()
                .map(authorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Author> findAllIncludingDeleted() {
        return authorJpaRepository.findAllIncludingDeleted().stream()
                .map(authorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Author> findByIdActive(UUID authorId) {
        return authorJpaRepository.findByIdActive(authorId)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Author> findByIdIncludingDeleted(UUID authorId) {
        return authorJpaRepository.findByIdIncludingDeleted(authorId)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Author> findByNameActive(String authorName) {
        return authorJpaRepository.findByNameActive(authorName)
                .map(authorPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID authorId) {
        return authorJpaRepository.existsByIdIncludingDeleted(authorId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String authorName) {
        return authorJpaRepository.existsByNameIncludingDeleted(authorName);
    }

    @Override
    public Author save(Author author) {
        AuthorJpaEntity entity = authorJpaRepository.findByIdIncludingDeleted(author.getId())
                .orElseGet(AuthorJpaEntity::new);
        authorPersistenceMapper.copyToEntity(entity, author);
        return authorPersistenceMapper.toDomain(authorJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID authorId) {
        authorJpaRepository.deleteById(authorId);
    }
}
