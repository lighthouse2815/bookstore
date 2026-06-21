package com.bookstore.mobile.feature.cart.data.dto

import com.bookstore.mobile.shared.model.Cart
import com.bookstore.mobile.shared.model.CartItem
import kotlinx.serialization.Serializable

@Serializable
data class CartDto(
    val cartId: String,
    val userId: String,
    val items: List<CartItemDto> = emptyList(),
    val totalQuantity: Int = 0,
    val totalAmount: Double = 0.0,
) {
    fun toModel(): Cart = Cart(
        id = cartId,
        userId = userId,
        items = items.map { it.toModel() },
        totalQuantity = totalQuantity,
        totalAmount = totalAmount,
    )
}

@Serializable
data class CartItemDto(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val imageUrl: String? = null,
    val price: Double,
    val quantity: Int,
    val lineTotal: Double,
) {
    fun toModel(): CartItem = CartItem(
        id = id,
        bookId = bookId,
        title = bookTitle,
        imageUrl = imageUrl,
        price = price,
        quantity = quantity,
        lineTotal = lineTotal,
    )
}
