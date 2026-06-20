package com.bookstore.mobile.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RequestRegistrationOtpRequest(
    val email: String,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otpCode: String,
)

@Serializable
data class RegisterResponse(
    val username: String,
    val createdAt: String,
)
