package com.bookstore.mobile.shared.model

data class Cart(
    val id: String,
    val userId: String,
    val items: List<CartItem>,
    val totalQuantity: Int,
    val totalAmount: Double,
)

data class CartItem(
    val id: String,
    val bookId: String,
    val title: String,
    val imageUrl: String?,
    val price: Double,
    val quantity: Int,
    val lineTotal: Double,
)
