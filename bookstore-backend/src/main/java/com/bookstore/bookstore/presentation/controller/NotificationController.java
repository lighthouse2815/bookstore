package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.application.result.NotificationSliceResult;
import com.bookstore.bookstore.presentation.mapper.NotificationWebMapper;
import com.bookstore.bookstore.presentation.request.BroadcastNotificationRequest;
import com.bookstore.bookstore.presentation.request.CreateNotificationRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.NotificationBroadcastResponse;
import com.bookstore.bookstore.presentation.response.NotificationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.bookstore.bookstore.presentation.response.UnreadNotificationCountResponse;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_PAGE = "X-Page";
    private static final String HEADER_SIZE = "X-Size";
    private static final String HEADER_HAS_NEXT = "X-Has-Next";

    private final INotificationService notificationService;
    private final NotificationWebMapper notificationWebMapper;

    @GetMapping("/api/notifications/my")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Boolean read
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        if (page != null || size != null) {
            NotificationSliceResult result = notificationService.getMyNotifications(
                    userId,
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? 10 : size
                    ),
                    read
            );
            return ResponseEntity.ok()
                    .headers(buildPaginationHeaders(result))
                    .body(ApiResponse.success(toResponses(result.items())));
        }

        return ResponseEntity.ok(ApiResponse.success(
                toResponses(notificationService.getMyNotifications(userId, read))
        ));
    }

    @GetMapping("/api/notifications/unread-count")
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(new UnreadNotificationCountResponse(
                notificationService.countMyUnreadNotifications(userId)
        ));
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

    @PutMapping("/api/notifications/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        notificationService.markAllRead(userId);
        return ApiResponse.success("Marked all as read", null);
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
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            NotificationSliceResult result = notificationService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            );
            return ResponseEntity.ok()
                    .headers(buildPaginationHeaders(result))
                    .body(ApiResponse.success(toResponses(result.items())));
        }

        return ResponseEntity.ok(ApiResponse.success(toResponses(notificationService.getAll())));
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/notifications/broadcast")
    public ResponseEntity<ApiResponse<NotificationBroadcastResponse>> broadcast(
            @Valid @RequestBody BroadcastNotificationRequest request
    ) {
        var result = notificationService.broadcast(notificationWebMapper.toBroadcastCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                new NotificationBroadcastResponse(result.createdCount())
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/notifications/{id}")
    public ApiResponse<Void> deleteByAdmin(@PathVariable UUID id) {
        notificationService.adminDelete(id);
        return ApiResponse.success("Deleted", null);
    }

    private List<NotificationResponse> toResponses(List<NotificationResult> results) {
        return results.stream()
                .map(notificationWebMapper::toResponse)
                .toList();
    }

    private HttpHeaders buildPaginationHeaders(NotificationSliceResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_TOTAL_COUNT, String.valueOf(result.totalCount()));
        headers.add(HEADER_PAGE, String.valueOf(result.page()));
        headers.add(HEADER_SIZE, String.valueOf(result.size()));
        headers.add(HEADER_HAS_NEXT, String.valueOf(result.hasNext()));
        return headers;
    }
}
