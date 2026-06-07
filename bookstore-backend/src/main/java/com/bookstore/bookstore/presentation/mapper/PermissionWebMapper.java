package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.presentation.request.UpdatePermissionRequest;
import com.bookstore.bookstore.presentation.response.PermissionResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PermissionWebMapper {

    public UpdatePermissionCommand toUpdateCommand(UUID permissionId, UpdatePermissionRequest request) {
        return new UpdatePermissionCommand(
                permissionId,
                request.code(),
                null
        );
    }

    public PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
