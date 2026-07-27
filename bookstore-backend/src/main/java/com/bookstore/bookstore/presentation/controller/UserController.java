package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserLockCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.UserWebMapper;
import com.bookstore.bookstore.presentation.request.CreateUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateStaffUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateUserRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.UserMeResponse;
import com.bookstore.bookstore.presentation.response.UserResponse;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserWebMapper userWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreateUserRequest request
    ) {
        var result = userService.createByAdmin(userWebMapper.toCreateCommand(request));
        UserResponse response = userWebMapper.toUserResponse(result);
        adminAuditSupport.recordCreate(
                jwt,
                httpServletRequest,
                "USER_CREATED",
                "USER",
                response.userId(),
                "Tạo tài khoản " + response.username(),
                response
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/customers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getCustomers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = userService.getCustomers(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(userWebMapper::toUserResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.getCustomers().stream()
                .map(userWebMapper::toUserResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStaffs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = userService.getStaffs(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(userWebMapper::toUserResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.getStaffs().stream()
                .map(userWebMapper::toUserResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/staff/{id}")
    public ApiResponse<UserResponse> updateStaff(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffUserRequest request
    ) {
        UserResponse before = userWebMapper.toUserResponse(userService.getByIdIncludingDeleted(id));
        var result = userService.updateStaffByAdmin(userWebMapper.toUpdateStaffCommand(id, request));
        UserResponse response = userWebMapper.toUserResponse(result);
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                "USER_UPDATED",
                "USER",
                response.userId(),
                "Cập nhật tài khoản " + response.username(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/{id}/lock")
    public ApiResponse<UserResponse> lockUser(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        UserResponse before = userWebMapper.toUserResponse(userService.getByIdIncludingDeleted(id));
        var result = userService.updateLockByAdmin(new UpdateUserLockCommand(id, adminId, true));
        UserResponse response = userWebMapper.toUserResponse(result);
        adminAuditSupport.recordStatusChange(
                jwt,
                httpServletRequest,
                "USER_LOCKED",
                "USER",
                response.userId(),
                "Khóa tài khoản " + response.username(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/users/{id}/unlock")
    public ApiResponse<UserResponse> unlockUser(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        UserResponse before = userWebMapper.toUserResponse(userService.getByIdIncludingDeleted(id));
        var result = userService.updateLockByAdmin(new UpdateUserLockCommand(id, adminId, false));
        UserResponse response = userWebMapper.toUserResponse(result);
        adminAuditSupport.recordStatusChange(
                jwt,
                httpServletRequest,
                "USER_UNLOCKED",
                "USER",
                response.userId(),
                "Mở khóa tài khoản " + response.username(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/admins")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAdmins(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = userService.getAdmins(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(userWebMapper::toUserResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.getAdmins().stream()
                .map(userWebMapper::toUserResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users/shippers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getShippers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = userService.getShippers(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(userWebMapper::toUserResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.getShippers().stream()
                .map(userWebMapper::toUserResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/users/{id}")
    public ApiResponse<Void> deleteByAdmin(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        UserResponse before = userWebMapper.toUserResponse(userService.getByIdIncludingDeleted(id));
        userService.deleteByAdmin(new DeleteUserCommand(id, adminId));
        adminAuditSupport.recordDelete(
                jwt,
                httpServletRequest,
                "USER_DELETED",
                "USER",
                id,
                "Xóa tài khoản " + before.username(),
                before
        );
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
