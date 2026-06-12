package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.Set;

public record UpdateStaffUserRequest(
        @Pattern(regexp = "^\\s*0\\d{9}\\s*$", message = "phoneNumber khong hop le")
        String phoneNumber,

        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email,

        @NotEmpty(message = "roleNames khong duoc de trong")
        Set<@NotBlank(message = "roleName khong duoc de trong") String> roleNames
) {
}
