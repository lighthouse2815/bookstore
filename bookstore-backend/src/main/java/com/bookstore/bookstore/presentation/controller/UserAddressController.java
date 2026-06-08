package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IUserAddressService;
import com.bookstore.bookstore.presentation.mapper.UserAddressWebMapper;
import com.bookstore.bookstore.presentation.request.CreateUserAddressRequest;
import com.bookstore.bookstore.presentation.request.UpdateUserAddressRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.UserAddressResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final IUserAddressService userAddressService;
    private final UserAddressWebMapper userAddressWebMapper;

    @GetMapping
    public ApiResponse<List<UserAddressResponse>> getMyAddresses(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(userAddressService.getMyAddresses(userId).stream()
                .map(userAddressWebMapper::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateUserAddressRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = userAddressService.create(userAddressWebMapper.toCreateCommand(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userAddressWebMapper.toResponse(result)));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAddressResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserAddressRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = userAddressService.update(userAddressWebMapper.toUpdateCommand(userId, id, request));
        return ApiResponse.success(userAddressWebMapper.toResponse(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userAddressService.delete(userAddressWebMapper.toDeleteCommand(userId, id));
        return ApiResponse.success("Deleted", null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<UserAddressResponse> setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = userAddressService.setDefault(userAddressWebMapper.toSetDefaultCommand(userId, id));
        return ApiResponse.success(userAddressWebMapper.toResponse(result));
    }
}
