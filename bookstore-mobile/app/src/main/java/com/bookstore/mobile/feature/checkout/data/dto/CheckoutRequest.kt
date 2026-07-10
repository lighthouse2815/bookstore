package com.bookstore.mobile.feature.checkout.data.dto

import com.bookstore.mobile.shared.model.BestCouponSuggestion
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

@Serializable
data class BestCouponSuggestionDto(
    val available: Boolean = false,
    val couponCode: String? = null,
    val couponType: String? = null,
    val discountAmount: Double = 0.0,
    val finalAmountEstimate: Double = 0.0,
    val label: String? = null,
    val reason: String? = null,
) {
    fun toModel(): BestCouponSuggestion = BestCouponSuggestion(
        available = available,
        couponCode = couponCode,
        couponType = couponType,
        discountAmount = discountAmount,
        finalAmountEstimate = finalAmountEstimate,
        label = label,
        reason = reason,
    )
}
