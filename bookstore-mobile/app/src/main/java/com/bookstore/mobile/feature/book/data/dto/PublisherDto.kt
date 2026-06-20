package com.bookstore.mobile.feature.book.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PublisherDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
