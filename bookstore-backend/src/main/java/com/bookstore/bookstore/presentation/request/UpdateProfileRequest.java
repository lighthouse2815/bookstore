package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileRequest(
        @NotBlank(message = "lastName không được để trống")
        String lastName,

        @NotBlank(message = "firstName không được để trống")
        String firstName,

        UUID avatarFileAssetId,

        @NotNull(message = "gender không được null")
        Gender gender,

        @NotNull(message = "dateOfBirth không được null")
        @PastOrPresent(message = "dateOfBirth không được nằm trong tương lai")
        LocalDate dateOfBirth
) {
}

