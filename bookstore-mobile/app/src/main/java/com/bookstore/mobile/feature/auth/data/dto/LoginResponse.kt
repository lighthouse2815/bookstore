package com.bookstore.mobile.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val userId: String,
    val status: String,
    val roles: List<String> = emptyList(),
    val accessToken: String,
    val refreshToken: String,
)
