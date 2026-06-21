package com.bookstore.mobile.shared.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val status: String,
    val roles: List<String>,
)
