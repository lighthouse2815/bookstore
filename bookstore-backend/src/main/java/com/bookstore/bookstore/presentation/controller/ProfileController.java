package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.presentation.mapper.ProfileWebMapper;
import com.bookstore.bookstore.presentation.request.UpdateProfileRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.ProfileResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;
    private final ProfileWebMapper profileWebMapper;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(profileWebMapper.toProfileResponse(profileService.getByUserId(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = profileService.update(profileWebMapper.toUpdateCommand(userId, request));
        return ResponseEntity.ok(ApiResponse.success(profileWebMapper.toProfileResponse(result)));
    }
}
