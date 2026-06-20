package com.bookstore.mobile.feature.cart.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val bookId: String,
    val quantity: Int,
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int,
)
