package com.bookstore.mobile.feature.profile.data.dto

import com.bookstore.mobile.shared.model.Address
import com.bookstore.mobile.shared.model.Profile
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val userId: String,
    val lastName: String? = "",
    val firstName: String? = "",
    val avatarUrl: String? = null,
    val gender: String? = "OTHER",
    val dateOfBirth: String? = "1990-01-01",
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toModel(): Profile = Profile(
        id = id,
        userId = userId,
        lastName = lastName.orEmpty(),
        firstName = firstName.orEmpty(),
        avatarUrl = avatarUrl,
        gender = gender ?: "OTHER",
        dateOfBirth = dateOfBirth ?: "1990-01-01",
    )
}

@Serializable
data class UpdateProfileRequest(
    val lastName: String,
    val firstName: String,
    val avatarUrl: String? = null,
    val gender: String,
    val dateOfBirth: String,
)

@Serializable
data class UpdateUserRequest(
    val username: String,
    val phoneNumber: String,
    val email: String,
)

@Serializable
data class UserAddressDto(
    val id: String,
    val userId: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val defaultAddress: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toModel(): Address = Address(
        id = id,
        receiverName = receiverName,
        receiverPhone = receiverPhone,
        receiverAddress = receiverAddress,
        defaultAddress = defaultAddress,
    )
}

@Serializable
data class CreateUserAddressRequest(
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
)
