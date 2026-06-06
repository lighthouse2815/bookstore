package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateRoleCommand;
import com.bookstore.bookstore.application.command.DeleteRoleCommand;
import com.bookstore.bookstore.application.command.UpdateRoleCommand;
import com.bookstore.bookstore.domain.model.Role;
import java.util.List;

public interface IRoleService {

    List<Role> getAll();

    Role create(CreateRoleCommand command);

    Role update(UpdateRoleCommand command);

    void delete(DeleteRoleCommand command);
}
