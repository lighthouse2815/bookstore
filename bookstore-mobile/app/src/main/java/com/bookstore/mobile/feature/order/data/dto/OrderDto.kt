package com.bookstore.mobile.feature.order.data.dto

import com.bookstore.mobile.shared.model.Order
import com.bookstore.mobile.shared.model.OrderItem
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val orderId: String,
    val orderCode: String,
    val userId: String,
    val items: List<OrderItemDto> = emptyList(),
    val productTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val shippingDiscount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val finalAmount: Double = totalAmount,
    val couponId: String? = null,
    val couponCode: String? = null,
    val bookCouponId: String? = null,
    val bookCouponCode: String? = null,
    val shippingCouponId: String? = null,
    val shippingCouponCode: String? = null,
    val paymentMethod: String,
    val paymentStatus: String,
    val status: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val createdAt: String,
    val updatedAt: String,
    val cancelledAt: String? = null,
    val paymentExpiresAt: String? = null,
) {
    fun toModel(): Order = Order(
        id = orderId,
        orderCode = orderCode,
        items = items.map { it.toModel() },
        productTotal = productTotal,
        shippingFee = shippingFee,
        shippingDiscount = shippingDiscount,
        couponDiscount = couponDiscount,
        totalAmount = finalAmount.takeIf { it > 0.0 } ?: totalAmount,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        status = status,
        receiverName = receiverName,
        receiverPhone = receiverPhone,
        receiverAddress = receiverAddress,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

@Serializable
data class OrderItemDto(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val unitPrice: Double,
    val quantity: Int,
    val lineTotal: Double,
) {
    fun toModel(): OrderItem = OrderItem(
        id = id,
        bookId = bookId,
        title = bookTitle,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )
}
