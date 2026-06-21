package com.bookstore.mobile.feature.auth.data.dto

import com.bookstore.mobile.shared.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val userId: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = "",
    val status: String,
    val locked: Boolean = false,
    val roles: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toModel(): User = User(
        id = userId,
        username = username,
        email = email,
        phoneNumber = phoneNumber.orEmpty(),
        status = status,
        roles = roles,
    )
}
