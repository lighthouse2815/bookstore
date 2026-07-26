package com.bookstore.mobile.shared.model

data class Order(
    val id: String,
    val orderCode: String,
    val items: List<OrderItem>,
    val productTotal: Double,
    val shippingFee: Double,
    val shippingDiscount: Double,
    val couponDiscount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val status: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val createdAt: String,
    val updatedAt: String,
)

data class OrderItem(
    val id: String,
    val bookId: String,
    val title: String,
    val unitPrice: Double,
    val quantity: Int,
    val lineTotal: Double,
)

data class CheckoutResult(
    val orderId: String,
    val orderCode: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val totalAmount: Double,
    val transferContent: String?,
    val orderStatus: String?,
)

data class BestCouponSuggestion(
    val available: Boolean,
    val couponCode: String?,
    val couponType: String?,
    val discountAmount: Double,
    val finalAmountEstimate: Double,
    val label: String?,
    val reason: String?,
)

data class OrderTimelineEvent(
    val id: String,
    val eventType: String,
    val title: String,
    val description: String?,
    val oldStatus: String?,
    val newStatus: String?,
    val actorName: String?,
    val actorRole: String?,
    val createdAt: String,
)
