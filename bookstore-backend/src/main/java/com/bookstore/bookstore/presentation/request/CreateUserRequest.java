package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank(message = "username khong duoc de trong")
        String username,

        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 8, max = 72, message = "password phai tu 8 den 72 ky tu")
        String password,

        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber khong hop le")
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
        LocalDate dateOfBirth,

        @NotBlank(message = "roleName khong duoc de trong")
        String roleName
) {
}
