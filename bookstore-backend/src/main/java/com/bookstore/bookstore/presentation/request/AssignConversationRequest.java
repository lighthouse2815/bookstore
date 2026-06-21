package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignConversationRequest(
        @NotNull(message = "staffId khong duoc null")
        UUID staffId
) {
}
