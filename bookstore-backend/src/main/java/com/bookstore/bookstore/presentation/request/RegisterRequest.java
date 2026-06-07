package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "username khong duoc de trong")
        String username,

        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 8, max = 72, message = "password phai tu 8 den 72 ky tu")
        String password,

        @NotBlank(message = "phoneNumber khong duoc de trong")
        String phoneNumber,

        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email,

        @NotBlank(message = "firstName khong duoc de trong")
        String firstName,

        @NotBlank(message = "lastName khong duoc de trong")
        String lastName,

        String avatarUrl,

        @NotNull(message = "gender khong duoc null")
        Gender gender,

        @NotNull(message = "dateOfBirth khong duoc null")
        @PastOrPresent(message = "dateOfBirth khong duoc nam trong tuong lai")
        LocalDate dateOfBirth
) {
}
