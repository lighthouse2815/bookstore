package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailRefundRequest(@NotBlank @Size(max = 1000) String failureReason) { }
