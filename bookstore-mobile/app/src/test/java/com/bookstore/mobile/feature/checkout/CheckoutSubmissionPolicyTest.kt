package com.bookstore.mobile.feature.checkout

import com.bookstore.mobile.shared.model.BestCouponSuggestion
import com.bookstore.mobile.shared.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutSubmissionPolicyTest {
    @Test
    fun `selected cart rows use cart item id rather than book id`() {
        val selected = selectedCheckoutItems(
            cartItems = listOf(cartItem(id = "cart-row-1", bookId = "book-1")),
            selectedCartItemIds = listOf("cart-row-1"),
        )

        assertEquals(listOf("cart-row-1"), selected.map { it.id })
    }

    @Test
    fun `existing address builds request without creating a new address`() {
        val ready = readyDecision(isCreatingNewAddress = false, selectedAddressId = "address-existing")

        assertTrue(ready.addressPlan is CheckoutAddressPlan.Existing)
        assertEquals("address-existing", (ready.addressPlan as CheckoutAddressPlan.Existing).addressId)
    }

    @Test
    fun `new address is only planned when create new is selected`() {
        val ready = readyDecision(isCreatingNewAddress = true, selectedAddressId = "address-existing")

        assertTrue(ready.addressPlan is CheckoutAddressPlan.New)
        assertEquals("12 Nguyen Trai, Ha Noi", (ready.addressPlan as CheckoutAddressPlan.New).address)
    }

    @Test
    fun `checkout request contains selected rows address shipping payment and coupon fields`() {
        val request = readyDecision(isCreatingNewAddress = false, selectedAddressId = "address-1")
            .plan
            .toRequest("address-1")

        assertEquals(listOf("cart-row-1"), request.cartItemIds)
        assertEquals("address-1", request.addressId)
        assertEquals("DELIVERY", request.shippingMethod)
        assertEquals("BANK_TRANSFER_QR", request.paymentMethod)
        assertEquals("BOOK10", request.bookCouponCode)
        assertEquals("SHIP20", request.shippingCouponCode)
    }

    @Test
    fun `checkout is rejected when no cart item is selected`() {
        val decision = buildCheckoutSubmission(
            isSubmitting = false,
            selectedItems = emptyList(),
            selectedAddressId = "address-1",
            isCreatingNewAddress = false,
            fullName = "",
            phone = "",
            addressDetail = "",
            provinceCity = "",
            shippingMethod = "DELIVERY",
            paymentMethod = "BANK_TRANSFER_QR",
            bookCouponCode = "",
            shippingCouponCode = "",
            note = "",
        )

        assertEquals(
            "Chon it nhat mot san pham de dat hang",
            (decision as CheckoutSubmissionDecision.Rejected).message,
        )
    }

    @Test
    fun `best book and shipping coupons update their corresponding fields`() {
        val initial = CheckoutCouponCodes(bookCouponCode = "", shippingCouponCode = "")
        val book = applyBestCouponSuggestion(initial, coupon("BOOK", "BOOK10"))
        val shipping = applyBestCouponSuggestion(book, coupon("SHIPPING", "SHIP20"))

        assertEquals("BOOK10", shipping.bookCouponCode)
        assertEquals("SHIP20", shipping.shippingCouponCode)
    }

    @Test
    fun `successful checkout falls back to pending when order detail is unavailable`() {
        assertEquals("PENDING", checkoutOrderStatusOrPending(null))
        assertEquals("PENDING", checkoutOrderStatusOrPending("  "))
        assertEquals("CONFIRMED", checkoutOrderStatusOrPending(" CONFIRMED "))
    }

    @Test
    fun `double submit is rejected while a submit is in progress`() {
        val decision = buildCheckoutSubmission(
            isSubmitting = true,
            selectedItems = listOf(cartItem()),
            selectedAddressId = "address-1",
            isCreatingNewAddress = false,
            fullName = "",
            phone = "",
            addressDetail = "",
            provinceCity = "",
            shippingMethod = "DELIVERY",
            paymentMethod = "BANK_TRANSFER_QR",
            bookCouponCode = "",
            shippingCouponCode = "",
            note = "",
        )

        assertTrue(decision is CheckoutSubmissionDecision.Rejected)
        assertEquals(null, (decision as CheckoutSubmissionDecision.Rejected).message)
    }

    private fun readyDecision(
        isCreatingNewAddress: Boolean,
        selectedAddressId: String?,
    ): CheckoutSubmissionDecision.Ready = buildCheckoutSubmission(
        isSubmitting = false,
        selectedItems = listOf(cartItem()),
        selectedAddressId = selectedAddressId,
        isCreatingNewAddress = isCreatingNewAddress,
        fullName = "Nguyen Van A",
        phone = "0900000000",
        addressDetail = "12 Nguyen Trai",
        provinceCity = "Ha Noi",
        shippingMethod = "DELIVERY",
        paymentMethod = "BANK_TRANSFER_QR",
        bookCouponCode = "BOOK10",
        shippingCouponCode = "SHIP20",
        note = "Giao gio hanh chinh",
    ) as CheckoutSubmissionDecision.Ready

    private fun cartItem(
        id: String = "cart-row-1",
        bookId: String = "book-1",
    ) = CartItem(
        id = id,
        bookId = bookId,
        title = "Clean Code",
        imageUrl = null,
        price = 100_000.0,
        quantity = 1,
        lineTotal = 100_000.0,
    )

    private fun coupon(type: String, code: String) = BestCouponSuggestion(
        available = true,
        couponCode = code,
        couponType = type,
        discountAmount = 10_000.0,
        finalAmountEstimate = 90_000.0,
        label = null,
        reason = null,
    )
}
