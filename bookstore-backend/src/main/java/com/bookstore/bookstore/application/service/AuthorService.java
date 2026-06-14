package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateAuthorCommand;
import com.bookstore.bookstore.application.command.DeleteAuthorCommand;
import com.bookstore.bookstore.application.command.UpdateAuthorCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuthorService;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorService implements IAuthorService {

    private final IAuthorRepository authorRepository;

    @Override
    public List<Author> getAll() {
        return authorRepository.findAllActive();
    }

    @Override
    public List<Author> getAllIncludingDeleted() {
        return authorRepository.findAllIncludingDeleted();
    }

    @Override
    public Author getById(UUID authorId) {
        if (authorId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "authorId");
        }

        return authorRepository.findByIdActive(authorId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));
    }

    @Override
    public Author getByIdIncludingDeleted(UUID authorId) {
        if (authorId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "authorId");
        }

        return authorRepository.findByIdIncludingDeleted(authorId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));
    }
 
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Author create(CreateAuthorCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String name = StringUtils.trimToNull(command.name());
        String biography = StringUtils.trimToNull(command.biography());
        String avatarUrl = StringUtils.trimToNull(command.avatarUrl());

        // bay gio khong cho trung ten, sau co the doi them 1 trường nhận biết
        if (authorRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.AUTHOR_NAME_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        Author author = new Author(
                UUID.randomUUID(),
                name,
                biography,
                avatarUrl,
                command.birthYear(),
                command.deathYear(),
                now,
                now,
                null
        );

        return authorRepository.save(author);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Author update(UpdateAuthorCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Author currentAuthor = authorRepository.findByIdActive(command.authorId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));

        String name = StringUtils.trimToNull(command.name());
        String biography = StringUtils.trimToNull(command.biography());
        String avatarUrl = StringUtils.trimToNull(command.avatarUrl());

        if (!currentAuthor.getName().equals(name) && authorRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.AUTHOR_NAME_ALREADY_EXISTS);
        }

        currentAuthor.updateAuthor(
                name,
                biography,
                avatarUrl,
                command.birthYear(),
                command.deathYear()
        );
        return authorRepository.save(currentAuthor);
    }
 
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteAuthorCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Author currentAuthor = authorRepository.findByIdActive(command.authorId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));

        currentAuthor.softDelete();
        authorRepository.save(currentAuthor);
    }
}
