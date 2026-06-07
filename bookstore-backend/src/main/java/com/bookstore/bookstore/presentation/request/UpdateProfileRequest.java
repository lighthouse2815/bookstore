package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "lastName khong duoc de trong")
        String lastName,

        @NotBlank(message = "firstName khong duoc de trong")
        String firstName,

        String avatarUrl,

        @NotNull(message = "gender khong duoc null")
        Gender gender,

        @NotNull(message = "dateOfBirth khong duoc null")
        @PastOrPresent(message = "dateOfBirth khong duoc nam trong tuong lai")
        LocalDate dateOfBirth
) {
}
