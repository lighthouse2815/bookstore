package com.bookstore.mobile.shared.model

data class Address(
    val id: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val defaultAddress: Boolean,
)

data class Profile(
    val id: String,
    val userId: String,
    val lastName: String,
    val firstName: String,
    val avatarUrl: String?,
    val gender: String,
    val dateOfBirth: String,
)
