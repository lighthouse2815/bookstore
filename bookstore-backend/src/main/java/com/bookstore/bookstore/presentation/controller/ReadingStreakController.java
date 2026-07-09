package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IReadingStreakService;
import com.bookstore.bookstore.presentation.mapper.ReadingJournalWebMapper;
import com.bookstore.bookstore.presentation.request.CheckInReadingStreakRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.ReadingStreakResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reading-streak")
@RequiredArgsConstructor
public class ReadingStreakController {

    private final IReadingStreakService readingStreakService;
    private final ReadingJournalWebMapper readingJournalWebMapper;

    @GetMapping
    public ApiResponse<ReadingStreakResponse> getMyStreak(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(readingJournalWebMapper.toStreakResponse(
                readingStreakService.getMyStreak(userId)
        ));
    }

    @PostMapping("/check-in")
    public ApiResponse<ReadingStreakResponse> checkIn(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckInReadingStreakRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(readingJournalWebMapper.toStreakResponse(
                readingStreakService.checkIn(readingJournalWebMapper.toCheckInCommand(userId, request))
        ));
    }
}
