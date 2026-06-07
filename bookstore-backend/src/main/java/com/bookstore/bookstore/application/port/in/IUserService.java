package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.domain.model.User;
import java.util.List;
import java.util.UUID;

public interface IUserService {

    List<User> getAll();

    List<User> getAllIncludingDeleted();

    User create(User user);

    User getById(UUID userId);

    User getByIdIncludingDeleted(UUID userId);

    User update(UpdateUserCommand command);

    void delete(DeleteUserCommand command);
}
