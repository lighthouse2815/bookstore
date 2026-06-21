package com.bookstore.mobile.feature.book.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookDto(
    val id: String,
    val title: String,
    val isbn: String? = null,
    val description: String? = null,
    val price: Double,
    val stockQuantity: Int,
    val soldCount: Int = 0,
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
    val starBreakdown: Map<String, Int> = emptyMap(),
    val imageUrl: String? = null,
    val images: List<BookImageDto> = emptyList(),
    val detail: BookDetailDto? = null,
    val categoryId: String,
    val authorId: String,
    val publisherId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class BookImageDto(
    val id: String,
    val bookId: String,
    val imageUrl: String,
    val primaryImage: Boolean = false,
    val sortOrder: Int = 0,
    val altText: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class BookDetailDto(
    val id: String? = null,
    val bookId: String? = null,
    val pageCount: Int? = null,
    val publicationYear: Int? = null,
    val language: String? = null,
    val coverType: String? = null,
    val dimensions: String? = null,
    val weight: Double? = null,
    val translator: String? = null,
    val edition: String? = null,
)
