package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateRoleCommand;
import com.bookstore.bookstore.application.command.DeleteRoleCommand;
import com.bookstore.bookstore.application.command.UpdateRoleCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IRoleService;
import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    @Override
    public List<Role> getAll() {
        return roleRepository.findAllActive();
    }

    @Override
    public List<Role> getAllIncludingDeleted() {
        return roleRepository.findAllIncludingDeleted();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(CreateRoleCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String roleName = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());

        if (roleRepository.existsByNameIncludingDeleted(roleName)) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        Set<Permission> permissions = toPermissions(command.permissionCodes());
        Instant now = Instant.now();
        Role role = new Role(
                UUID.randomUUID(),
                roleName,
                description,
                permissions,
                now,
                now,
                null
        );

        return roleRepository.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(UpdateRoleCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Role currentRole = roleRepository.findByIdActive(command.roleId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));

        String roleName = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());
        Set<Permission> permissions = toPermissions(command.permissionCodes());

        if (!currentRole.getName().equals(roleName) && roleRepository.existsByNameIncludingDeleted(roleName)) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        currentRole.updateRole(roleName, description, permissions);
        return roleRepository.save(currentRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteRoleCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Role currentRole = roleRepository.findByIdActive(command.roleId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));

        currentRole.softDelete();
        roleRepository.save(currentRole);
    }

    private Set<Permission> toPermissions(Set<PermissionCode> permissionCodes) {
        if (permissionCodes == null) {
            return new LinkedHashSet<>();
        }

        return permissionCodes.stream()
                .map(permissionCode -> permissionRepository.findByCodeActive(permissionCode)
                        .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
