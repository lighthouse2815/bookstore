package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserLockCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.presentation.mapper.UserWebMapper;
import com.bookstore.bookstore.presentation.request.CreateUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateStaffUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateUserRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.UserMeResponse;
import com.bookstore.bookstore.presentation.response.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserWebMapper userWebMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        var result = userService.createByAdmin(userWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userWebMapper.toUserResponse(result)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/customers")
    public ApiResponse<List<UserResponse>> getCustomers() {
        return ApiResponse.success(userService.getCustomers().stream()
                .map(userWebMapper::toUserResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/staff")
    public ApiResponse<List<UserResponse>> getStaffs() {
        return ApiResponse.success(userService.getStaffs().stream()
                .map(userWebMapper::toUserResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/staff/{id}")
    public ApiResponse<UserResponse> updateStaff(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffUserRequest request
    ) {
        var result = userService.updateStaffByAdmin(userWebMapper.toUpdateStaffCommand(id, request));
        return ApiResponse.success(userWebMapper.toUserResponse(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/{id}/lock")
    public ApiResponse<UserResponse> lockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        var result = userService.updateLockByAdmin(new UpdateUserLockCommand(id, adminId, true));
        return ApiResponse.success(userWebMapper.toUserResponse(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/{id}/unlock")
    public ApiResponse<UserResponse> unlockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        var result = userService.updateLockByAdmin(new UpdateUserLockCommand(id, adminId, false));
        return ApiResponse.success(userWebMapper.toUserResponse(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/admins")
    public ApiResponse<List<UserResponse>> getAdmins() {
        return ApiResponse.success(userService.getAdmins().stream()
                .map(userWebMapper::toUserResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/shippers")
    public ApiResponse<List<UserResponse>> getShippers() {
        return ApiResponse.success(userService.getShippers().stream()
                .map(userWebMapper::toUserResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/users/{id}")
    public ApiResponse<Void> deleteByAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        userService.deleteByAdmin(new DeleteUserCommand(id, adminId));
        return ApiResponse.success("Deleted", null);
    }

    @GetMapping("/api/users/me")
    public ApiResponse<UserMeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(userWebMapper.toUserMeResponse(userService.getById(userId)));
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UpdateUserCommand command = userWebMapper.toUpdateCommand(userId, request);
        return ResponseEntity.ok(ApiResponse.success(userWebMapper.toUserMeResponse(userService.update(command))));
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<ApiResponse<Void>> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.delete(new DeleteUserCommand(userId, userId));
        return ResponseEntity.ok(ApiResponse.<Void>success("Deleted", null));
    }
}
