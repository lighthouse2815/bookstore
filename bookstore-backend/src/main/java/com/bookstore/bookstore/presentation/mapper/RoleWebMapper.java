package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateRoleCommand;
import com.bookstore.bookstore.application.command.DeleteRoleCommand;
import com.bookstore.bookstore.application.command.UpdateRoleCommand;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.presentation.request.CreateRoleRequest;
import com.bookstore.bookstore.presentation.request.UpdateRoleRequest;
import com.bookstore.bookstore.presentation.response.RoleResponse;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RoleWebMapper {

    public CreateRoleCommand toCreateCommand(CreateRoleRequest request) {
        return new CreateRoleCommand(
                request.name(),
                request.description(),
                request.permissionCodes()
        );
    }

    public UpdateRoleCommand toUpdateCommand(UUID roleId, UpdateRoleRequest request) {
        return new UpdateRoleCommand(
                roleId,
                request.name(),
                request.description(),
                request.permissionCodes()
        );
    }

    public DeleteRoleCommand toDeleteCommand(UUID roleId) {
        return new DeleteRoleCommand(roleId);
    }

    public RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(permission -> permission.getCode())
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
