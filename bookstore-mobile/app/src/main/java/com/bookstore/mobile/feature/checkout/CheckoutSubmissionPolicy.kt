package com.bookstore.mobile.feature.checkout

import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import com.bookstore.mobile.shared.model.BestCouponSuggestion
import com.bookstore.mobile.shared.model.CartItem

sealed interface CheckoutAddressPlan {
    data class Existing(val addressId: String) : CheckoutAddressPlan

    data class New(
        val fullName: String,
        val phone: String,
        val address: String,
    ) : CheckoutAddressPlan
}

data class CheckoutSubmissionPlan(
    val cartItemIds: List<String>,
    val shippingMethod: String,
    val paymentMethod: String,
    val bookCouponCode: String,
    val shippingCouponCode: String,
    val note: String,
) {
    fun toRequest(addressId: String): CheckoutRequest = CheckoutRequest(
        cartItemIds = cartItemIds,
        addressId = addressId,
        shippingMethod = shippingMethod,
        paymentMethod = paymentMethod,
        bookCouponCode = bookCouponCode,
        shippingCouponCode = shippingCouponCode,
        note = note,
    )
}

sealed interface CheckoutSubmissionDecision {
    data class Ready(
        val plan: CheckoutSubmissionPlan,
        val addressPlan: CheckoutAddressPlan,
    ) : CheckoutSubmissionDecision

    data class Rejected(val message: String? = null) : CheckoutSubmissionDecision
}

data class CheckoutCouponCodes(
    val bookCouponCode: String,
    val shippingCouponCode: String,
)

fun selectedCheckoutItems(
    cartItems: List<CartItem>,
    selectedCartItemIds: List<String>,
): List<CartItem> {
    val selectedIds = selectedCartItemIds.toSet()
    return cartItems.filter { it.id in selectedIds }
}

fun buildCheckoutSubmission(
    isSubmitting: Boolean,
    selectedItems: List<CartItem>,
    selectedAddressId: String?,
    isCreatingNewAddress: Boolean,
    fullName: String,
    phone: String,
    addressDetail: String,
    provinceCity: String,
    shippingMethod: String,
    paymentMethod: String,
    bookCouponCode: String,
    shippingCouponCode: String,
    note: String,
): CheckoutSubmissionDecision {
    if (isSubmitting) return CheckoutSubmissionDecision.Rejected()
    if (selectedItems.isEmpty()) {
        return CheckoutSubmissionDecision.Rejected("Chon it nhat mot san pham de dat hang")
    }

    val addressPlan = if (isCreatingNewAddress) {
        if (fullName.isBlank() || phone.isBlank() || addressDetail.isBlank()) {
            return CheckoutSubmissionDecision.Rejected("Nhap day du thong tin dia chi giao hang moi")
        }
        CheckoutAddressPlan.New(
            fullName = fullName,
            phone = phone,
            address = listOf(addressDetail, provinceCity)
                .filter { it.isNotBlank() }
                .joinToString(", "),
        )
    } else {
        val addressId = selectedAddressId
            ?: return CheckoutSubmissionDecision.Rejected("Chon dia chi giao hang hoac tao dia chi moi")
        CheckoutAddressPlan.Existing(addressId)
    }

    return CheckoutSubmissionDecision.Ready(
        plan = CheckoutSubmissionPlan(
            cartItemIds = selectedItems.map { it.id },
            shippingMethod = shippingMethod,
            paymentMethod = paymentMethod,
            bookCouponCode = bookCouponCode,
            shippingCouponCode = shippingCouponCode,
            note = note,
        ),
        addressPlan = addressPlan,
    )
}

fun applyBestCouponSuggestion(
    current: CheckoutCouponCodes,
    suggestion: BestCouponSuggestion?,
): CheckoutCouponCodes {
    val couponCode = suggestion?.couponCode ?: return current
    return when (suggestion.couponType) {
        "BOOK" -> current.copy(bookCouponCode = couponCode)
        "SHIPPING" -> current.copy(shippingCouponCode = couponCode)
        else -> current
    }
}

fun checkoutOrderStatusOrPending(orderStatus: String?): String =
    orderStatus?.trim()?.takeIf { it.isNotEmpty() } ?: "PENDING"
