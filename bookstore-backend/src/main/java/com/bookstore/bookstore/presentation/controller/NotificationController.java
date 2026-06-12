package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.presentation.mapper.NotificationWebMapper;
import com.bookstore.bookstore.presentation.request.CreateNotificationRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.NotificationResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final NotificationWebMapper notificationWebMapper;

    @GetMapping("/api/notifications/my")
    public ApiResponse<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(notificationService.getMyNotifications(userId).stream()
                .map(notificationWebMapper::toResponse)
                .toList());
    }

    @GetMapping("/api/notifications/{id}")
    public ApiResponse<NotificationResponse> getMyNotification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(notificationWebMapper.toResponse(notificationService.getMyNotification(userId, id)));
    }

    @PutMapping("/api/notifications/{id}/read")
    public ApiResponse<NotificationResponse> markRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(notificationWebMapper.toResponse(
                notificationService.markRead(notificationWebMapper.toMarkReadCommand(id, userId))
        ));
    }

    @DeleteMapping("/api/notifications/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationService.delete(notificationWebMapper.toDeleteCommand(id, userId));
        return ApiResponse.success("Deleted", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/notifications")
    public ApiResponse<List<NotificationResponse>> getAll() {
        return ApiResponse.success(notificationService.getAll().stream()
                .map(notificationWebMapper::toResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/notifications/{id}")
    public ApiResponse<NotificationResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(notificationWebMapper.toResponse(notificationService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/notifications")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationWebMapper.toResponse(
                notificationService.create(notificationWebMapper.toCreateCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
