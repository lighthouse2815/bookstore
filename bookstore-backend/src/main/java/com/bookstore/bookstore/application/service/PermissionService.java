package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPermissionService;
import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    public final IPermissionRepository permissionRepository;

    @Override
    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdatePermissionCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permission");
        }

        UUID permissionId = command.permissionId();
        PermissionCode code = command.code();

        Permission currentPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND));

        if (permissionRepository.existsByCode(code)) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS);
        }

        currentPermission.updatePermission(code);

        permissionRepository.save(currentPermission);
    }

   
}
