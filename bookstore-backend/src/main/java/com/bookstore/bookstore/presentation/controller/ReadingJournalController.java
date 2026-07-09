package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IReadingJournalService;
import com.bookstore.bookstore.presentation.mapper.ReadingJournalWebMapper;
import com.bookstore.bookstore.presentation.request.CreateReadingJournalEntryRequest;
import com.bookstore.bookstore.presentation.request.UpdateReadingJournalEntryRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.ReadingJournalEntryResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reading-journal")
@RequiredArgsConstructor
public class ReadingJournalController {

    private final IReadingJournalService readingJournalService;
    private final ReadingJournalWebMapper readingJournalWebMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingJournalEntryResponse>>> getMyEntries(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = readingJournalService.getMyEntries(
                userId,
                bookId,
                from,
                to,
                page == null ? 0 : page,
                size == null ? 12 : size
        ).map(readingJournalWebMapper::toResponse);

        return ResponseEntity.ok()
                .headers(PaginationHeaderUtils.build(result))
                .body(ApiResponse.success(result.items()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReadingJournalEntryResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReadingJournalEntryRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = readingJournalService.create(readingJournalWebMapper.toCreateCommand(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(readingJournalWebMapper.toResponse(result)));
    }

    @PutMapping("/{entryId}")
    public ApiResponse<ReadingJournalEntryResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateReadingJournalEntryRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(readingJournalWebMapper.toResponse(
                readingJournalService.update(readingJournalWebMapper.toUpdateCommand(entryId, userId, request))
        ));
    }

    @DeleteMapping("/{entryId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID entryId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        readingJournalService.delete(readingJournalWebMapper.toDeleteCommand(entryId, userId));
        return ApiResponse.success("Deleted", null);
    }
}
