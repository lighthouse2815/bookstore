package com.bookstore.mobile.shared.model

data class Book(
    val id: String,
    val title: String,
    val isbn: String?,
    val description: String?,
    val price: Double,
    val stockQuantity: Int,
    val soldCount: Int,
    val rating: Double?,
    val reviewCount: Int,
    val coverUrl: String?,
    val author: String,
    val category: String,
    val publisher: String,
    val images: List<String> = emptyList(),
    val detail: BookDetail? = null,
)

data class BookDetail(
    val pageCount: Int?,
    val publicationYear: Int?,
    val language: String?,
    val coverType: String?,
    val dimensions: String?,
    val weight: Double?,
    val translator: String?,
    val edition: String?,
)

data class Category(
    val id: String,
    val name: String,
)
