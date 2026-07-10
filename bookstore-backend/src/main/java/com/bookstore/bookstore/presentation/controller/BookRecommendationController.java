package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IPersonalizedRecommendationService;
import com.bookstore.bookstore.application.result.PersonalizedRecommendationResult;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PersonalizedRecommendationResponse;
import com.bookstore.bookstore.presentation.response.RecommendedBookResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/recommendations")
public class BookRecommendationController {

    private final IPersonalizedRecommendationService personalizedRecommendationService;
    private final BookWebMapper bookWebMapper;

    @GetMapping("/personalized")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PersonalizedRecommendationResponse> getPersonalized(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "12") @Min(1) @Max(24) int limit
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(toResponse(personalizedRecommendationService.getForUser(userId, limit)));
    }

    private PersonalizedRecommendationResponse toResponse(PersonalizedRecommendationResult result) {
        return new PersonalizedRecommendationResponse(
                result.items().stream()
                        .map(item -> new RecommendedBookResponse(
                                bookWebMapper.toBookResponse(item.book()),
                                item.reasonCodes().stream().map(Enum::name).toList()
                        ))
                        .toList(),
                result.strategy().name(),
                result.hasPersonalSignals(),
                result.generatedAt()
        );
    }
}
