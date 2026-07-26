package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank(message = "username không được để trống")
        String username,

        @NotBlank(message = "password không được để trống")
        @Size(min = 8, max = 72, message = "password phải tu 8 den 72 ký tự")
        String password,

        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,

        @NotBlank(message = "firstName không được để trống")
        String firstName,

        @NotBlank(message = "lastName không được để trống")
        String lastName,

        UUID avatarFileAssetId,

        @NotNull(message = "gender không được null")
        Gender gender,

        @NotNull(message = "dateOfBirth không được null")
        @PastOrPresent(message = "dateOfBirth không được nằm trong tương lai")
        LocalDate dateOfBirth,

        @NotBlank(message = "roleName không được để trống")
        String roleName
) {
}

