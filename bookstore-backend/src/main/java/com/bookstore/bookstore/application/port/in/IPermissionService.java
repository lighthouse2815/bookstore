package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.domain.model.Permission;
import java.util.List;

public interface IPermissionService {

    List<Permission> getAll();

    void update(UpdatePermissionCommand command);

}
