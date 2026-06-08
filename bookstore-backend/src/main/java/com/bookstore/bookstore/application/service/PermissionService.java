package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPermissionService;
import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    private final IPermissionRepository permissionRepository;

    @Override
    public List<Permission> getAll() {
        return permissionRepository.findAllActive();
    }

    @Override
    public List<Permission> getAllIncludingDeleted() {
        return permissionRepository.findAllIncludingDeleted();
    }

    @Override
    public Permission getById(UUID permissionId) {
        if (permissionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permissionId");
        }

        return permissionRepository.findByIdActive(permissionId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND));
    }

    @Override
    public Permission getByIdIncludingDeleted(UUID permissionId) {
        if (permissionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permissionId");
        }

        return permissionRepository.findByIdIncludingDeleted(permissionId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission update(UpdatePermissionCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permission");
        }

        UUID permissionId = command.permissionId();
        PermissionCode code = command.code();

        Permission currentPermission = permissionRepository.findByIdActive(permissionId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND));
        String description = StringUtils.trimToNull(command.description());
        

        if (!currentPermission.getCode().equals(code) && permissionRepository.existsByCodeIncludingDeleted(code)) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS);
        }

        currentPermission.updatePermission(code, description);

        return permissionRepository.save(currentPermission);
    }
}
