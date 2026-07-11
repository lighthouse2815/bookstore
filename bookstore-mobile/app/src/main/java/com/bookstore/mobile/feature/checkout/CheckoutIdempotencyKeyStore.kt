package com.bookstore.mobile.feature.checkout

import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import java.util.UUID

class CheckoutIdempotencyKeyStore {
    private var pendingRequest: CheckoutRequest? = null
    private var pendingKey: String? = null

    fun keyFor(request: CheckoutRequest): String {
        if (request == pendingRequest && pendingKey != null) {
            return pendingKey!!
        }

        return UUID.randomUUID().toString().also { key ->
            pendingRequest = request
            pendingKey = key
        }
    }

    fun clear() {
        pendingRequest = null
        pendingKey = null
    }
}
