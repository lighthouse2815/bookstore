package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateAuthorCommand;
import com.bookstore.bookstore.application.command.DeleteAuthorCommand;
import com.bookstore.bookstore.application.command.UpdateAuthorCommand;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IAuthorService {

    List<Author> getAll();

    PageSliceResult<Author> getAll(int page, int size);

    List<Author> getAllIncludingDeleted();

    Author getById(UUID authorId);

    Author getByIdIncludingDeleted(UUID authorId);

    Author create(CreateAuthorCommand command);

    Author update(UpdateAuthorCommand command);

    void delete(DeleteAuthorCommand command);
}
