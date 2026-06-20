package com.bookstore.mobile.feature.book.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookPageDetailDto(
    val book: BookPageDetailBookDto,
    val author: AuthorDto,
    val publisher: PublisherDto,
    val categoryTrail: List<CategoryTrailItemDto> = emptyList(),
    val ratingSummary: BookRatingSummaryDto,
    val promotions: List<BookPromotionDto> = emptyList(),
    val relatedBooks: List<BookDto> = emptyList(),
)

@Serializable
data class BookPageDetailBookDto(
    val id: String,
    val title: String,
    val isbn: String? = null,
    val price: Double,
    val originalPrice: Double? = null,
    val discountPercent: Double? = null,
    val stockQuantity: Int,
    val soldCount: Int = 0,
    val description: String? = null,
    val images: List<BookImageDto> = emptyList(),
    val detail: BookDetailDto? = null,
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
)

@Serializable
data class CategoryTrailItemDto(
    val id: String,
    val name: String,
)

@Serializable
data class BookRatingSummaryDto(
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
    val starBreakdown: Map<String, Int> = emptyMap(),
)

@Serializable
data class BookPromotionDto(
    val id: String,
    val code: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: Double,
    val minOrderAmount: Double? = null,
    val maxDiscountAmount: Double? = null,
    val maxUsageCount: Int? = null,
    val usedCount: Int = 0,
    val startsAt: String? = null,
    val expiresAt: String? = null,
    val active: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
