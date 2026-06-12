package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IRoleService;
import com.bookstore.bookstore.presentation.mapper.RoleWebMapper;
import com.bookstore.bookstore.presentation.request.CreateRoleRequest;
import com.bookstore.bookstore.presentation.request.UpdateRoleRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.RoleResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final IRoleService roleService;
    private final RoleWebMapper roleWebMapper;

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.success(roleService.getAll().stream()
                .map(roleWebMapper::toRoleResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        var result = roleService.create(roleWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleWebMapper.toRoleResponse(result)));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        var result = roleService.update(roleWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(roleWebMapper.toRoleResponse(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roleService.delete(roleWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
