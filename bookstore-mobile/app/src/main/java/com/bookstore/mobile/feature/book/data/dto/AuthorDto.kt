package com.bookstore.mobile.feature.book.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthorDto(
    val id: String,
    val name: String,
    val biography: String? = null,
    val avatarUrl: String? = null,
    val birthYear: Int? = null,
    val deathYear: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
