package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAuthorRepository {

    List<Author> findAllActive();

    PageSliceResult<Author> findPageActive(int page, int size);

    List<Author> findAllIncludingDeleted();

    Optional<Author> findByIdActive(UUID authorId);

    Optional<Author> findByIdIncludingDeleted(UUID authorId);

    Optional<Author> findByNameActive(String authorName);

    boolean existsByIdIncludingDeleted(UUID authorId);

    boolean existsByNameIncludingDeleted(String authorName);

    Author save(Author author);

    void deleteById(UUID authorId);
}
