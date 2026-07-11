package com.bookstore.mobile.feature.checkout

import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CheckoutIdempotencyKeyStoreTest {
    @Test
    fun `keeps the same key when retrying the same checkout payload`() {
        val store = CheckoutIdempotencyKeyStore()
        val request = request(note = "Giao giờ hành chính")

        assertEquals(store.keyFor(request), store.keyFor(request))
    }

    @Test
    fun `creates a fresh key after the checkout payload changes`() {
        val store = CheckoutIdempotencyKeyStore()

        val firstKey = store.keyFor(request(note = null))
        val secondKey = store.keyFor(request(note = "Gọi trước khi giao"))

        assertNotEquals(firstKey, secondKey)
    }

    private fun request(note: String?) = CheckoutRequest(
        cartItemIds = listOf("cart-item-1"),
        addressId = "address-1",
        paymentMethod = "BANK_TRANSFER_QR",
        note = note,
    )
}
