package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Size;

public record CancelRefundRequest(@Size(max = 1000) String reason) { }
