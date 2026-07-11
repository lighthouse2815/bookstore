package com.bookstore.mobile.feature.order.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CancelOrderRequest(
    val reason: String,
)
