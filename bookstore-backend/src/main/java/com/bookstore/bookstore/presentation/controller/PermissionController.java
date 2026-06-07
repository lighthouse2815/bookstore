package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IPermissionService;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.presentation.mapper.PermissionWebMapper;
import com.bookstore.bookstore.presentation.request.UpdatePermissionRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PermissionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;
    private final PermissionWebMapper permissionWebMapper;

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.success(permissionService.getAll().stream()
                .map(permissionWebMapper::toPermissionResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        Permission updatedPermission = permissionService.update(permissionWebMapper.toUpdateCommand(id, request));
        return ResponseEntity.ok(ApiResponse.success(permissionWebMapper.toPermissionResponse(updatedPermission)));
    }
}
