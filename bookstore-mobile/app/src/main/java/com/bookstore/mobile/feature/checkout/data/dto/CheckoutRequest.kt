package com.bookstore.mobile.feature.checkout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequest(
    val cartItemIds: List<String> = emptyList(),
    val addressId: String,
    val shippingMethod: String = "DELIVERY",
    val paymentMethod: String = "BANK_TRANSFER_QR",
    val bookCouponCode: String? = null,
    val shippingCouponCode: String? = null,
    val note: String? = null,
)

@Serializable
data class CheckoutResponse(
    val orderId: String,
    val orderCode: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val totalAmount: Double,
    val transferContent: String? = null,
)
