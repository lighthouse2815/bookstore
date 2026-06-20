package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IChatService;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ChatMessageSliceResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.application.result.ConversationSliceResult;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.presentation.mapper.ChatWebMapper;
import com.bookstore.bookstore.presentation.request.AssignConversationRequest;
import com.bookstore.bookstore.presentation.request.CreateConversationRequest;
import com.bookstore.bookstore.presentation.request.SendChatMessageRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.ChatMessageResponse;
import com.bookstore.bookstore.presentation.response.ConversationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_PAGE = "X-Page";
    private static final String HEADER_SIZE = "X-Size";
    private static final String HEADER_HAS_NEXT = "X-Has-Next";

    private final IChatService chatService;
    private final ChatWebMapper chatWebMapper;

    @GetMapping("/api/chat/conversations/my")
    public ApiResponse<List<ConversationResponse>> getMyConversations(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(toConversationResponses(chatService.getMyConversations(userId)));
    }

    @PostMapping("/api/chat/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateConversationRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ConversationResult result = chatService.createConversation(
                chatWebMapper.toCreateConversationCommand(request, userId)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatWebMapper.toConversationResponse(result)));
    }

    @GetMapping("/api/chat/conversations/{conversationId}")
    public ApiResponse<ConversationResponse> getConversationDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.getConversationDetail(userId, conversationId)
        ));
    }

    @GetMapping("/api/chat/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChatMessageSliceResult result = chatService.getMessages(userId, conversationId, page, size);
        return ResponseEntity.ok()
                .headers(buildPaginationHeaders(result))
                .body(ApiResponse.success(toMessageResponses(result.items())));
    }

    @PostMapping("/api/chat/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChatMessageResult result = chatService.sendUserMessage(
                chatWebMapper.toSendMessageCommand(conversationId, userId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatWebMapper.toMessageResponse(result)));
    }

    @PutMapping("/api/chat/conversations/{conversationId}/read")
    public ApiResponse<ConversationResponse> markConversationRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.markConversationRead(userId, conversationId)
        ));
    }

    @PutMapping("/api/chat/conversations/{conversationId}/close")
    public ApiResponse<ConversationResponse> closeMyConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.closeMyConversation(userId, conversationId)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/api/admin/chat/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAdminConversations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(required = false) String keyword
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ConversationSliceResult result = chatService.getAdminConversations(userId, status, keyword, page, size);
        return ResponseEntity.ok()
                .headers(buildPaginationHeaders(result))
                .body(ApiResponse.success(toConversationResponses(result.items())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/api/admin/chat/conversations/{conversationId}")
    public ApiResponse<ConversationResponse> getAdminConversationDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.getAdminConversationDetail(userId, conversationId)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/api/admin/chat/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getAdminMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChatMessageSliceResult result = chatService.getAdminMessages(userId, conversationId, page, size);
        return ResponseEntity.ok()
                .headers(buildPaginationHeaders(result))
                .body(ApiResponse.success(toMessageResponses(result.items())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/api/admin/chat/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendAdminMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ChatMessageResult result = chatService.sendAdminMessage(
                chatWebMapper.toSendMessageCommand(conversationId, userId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatWebMapper.toMessageResponse(result)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/api/admin/chat/conversations/{conversationId}/assign")
    public ApiResponse<ConversationResponse> assignConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId,
            @Valid @RequestBody AssignConversationRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.assignConversation(
                        chatWebMapper.toAssignConversationCommand(conversationId, userId, request)
                )
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/api/admin/chat/conversations/{conversationId}/read")
    public ApiResponse<ConversationResponse> markAdminConversationRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.markAdminConversationRead(userId, conversationId)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/api/admin/chat/conversations/{conversationId}/close")
    public ApiResponse<ConversationResponse> closeConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.closeConversation(userId, conversationId)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/api/admin/chat/conversations/{conversationId}/reopen")
    public ApiResponse<ConversationResponse> reopenConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID conversationId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(chatWebMapper.toConversationResponse(
                chatService.reopenConversation(userId, conversationId)
        ));
    }

    private List<ConversationResponse> toConversationResponses(List<ConversationResult> results) {
        return results.stream()
                .map(chatWebMapper::toConversationResponse)
                .toList();
    }

    private List<ChatMessageResponse> toMessageResponses(List<ChatMessageResult> results) {
        return results.stream()
                .map(chatWebMapper::toMessageResponse)
                .toList();
    }

    private HttpHeaders buildPaginationHeaders(ConversationSliceResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_TOTAL_COUNT, Long.toString(result.totalCount()));
        headers.add(HEADER_PAGE, Integer.toString(result.page()));
        headers.add(HEADER_SIZE, Integer.toString(result.size()));
        headers.add(HEADER_HAS_NEXT, Boolean.toString(result.hasNext()));
        return headers;
    }

    private HttpHeaders buildPaginationHeaders(ChatMessageSliceResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_TOTAL_COUNT, Long.toString(result.totalCount()));
        headers.add(HEADER_PAGE, Integer.toString(result.page()));
        headers.add(HEADER_SIZE, Integer.toString(result.size()));
        headers.add(HEADER_HAS_NEXT, Boolean.toString(result.hasNext()));
        return headers;
    }
}
