package com.bookstore.mobile.feature.cart.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.cart.data.dto.AddToCartRequest
import com.bookstore.mobile.feature.cart.data.dto.UpdateCartItemRequest
import com.bookstore.mobile.shared.model.Cart

class CartRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getCart(): ResultState<Cart> = call("Khong lay duoc gio hang") {
        apiClient.service().getCart().data?.toModel() ?: error("Gio hang khong hop le")
    }

    suspend fun addItem(bookId: String, quantity: Int): ResultState<Cart> = call("Khong them duoc vao gio hang") {
        apiClient.service().addToCart(AddToCartRequest(bookId, quantity)).data?.toModel()
            ?: error("Gio hang khong hop le")
    }

    suspend fun updateItem(bookId: String, quantity: Int): ResultState<Cart> = call("Khong cap nhat duoc so luong") {
        require(quantity > 0) { "So luong phai lon hon 0" }
        apiClient.service().updateCartItem(bookId, UpdateCartItemRequest(quantity)).data?.toModel()
            ?: error("Gio hang khong hop le")
    }

    suspend fun removeItem(bookId: String): ResultState<Unit> = call("Khong xoa duoc san pham") {
        apiClient.service().removeCartItem(bookId)
        Unit
    }

    suspend fun clear(): ResultState<Unit> = call("Khong xoa duoc gio hang") {
        apiClient.service().clearCart()
        Unit
    }

    private suspend fun <T> call(
        fallback: String,
        block: suspend () -> T,
    ): ResultState<T> = runCatching { block() }
        .fold(
            onSuccess = { ResultState.Success(it) },
            onFailure = { ResultState.Error(apiClient.errorMessage(it, fallback)) },
        )
}
