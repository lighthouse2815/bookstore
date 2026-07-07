package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.Set;

public record UpdateStaffUserRequest(
        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,

        @NotEmpty(message = "roleNames không được để trống")
        Set<@NotBlank(message = "roleName không được để trống") String> roleNames
) {
}

