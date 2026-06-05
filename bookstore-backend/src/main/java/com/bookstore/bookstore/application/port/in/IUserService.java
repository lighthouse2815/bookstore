package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.domain.model.User;
import java.util.List;

public interface IUserService {

    List<User> getAll();

    User create(User user);

    User update(UpdateUserCommand command);

    void delete(DeleteUserCommand command);
}
