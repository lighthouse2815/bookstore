package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.domain.model.Permission;
import java.util.List;
import java.util.UUID;

public interface IPermissionService {

    List<Permission> getAll();

    List<Permission> getAllIncludingDeleted();

    Permission getById(UUID permissionId);

    Permission getByIdIncludingDeleted(UUID permissionId);

    Permission update(UpdatePermissionCommand command);

}
