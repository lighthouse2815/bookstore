package com.bookstore.mobile.shared.model

data class Order(
    val id: String,
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
)
