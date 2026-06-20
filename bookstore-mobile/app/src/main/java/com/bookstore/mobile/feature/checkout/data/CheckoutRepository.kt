package com.bookstore.mobile.feature.checkout.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import com.bookstore.mobile.feature.profile.data.dto.CreateUserAddressRequest
import com.bookstore.mobile.shared.model.Address
import com.bookstore.mobile.shared.model.Cart
import com.bookstore.mobile.shared.model.CheckoutResult

class CheckoutRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getCart(): ResultState<Cart> = call("Khong lay duoc gio hang") {
        apiClient.service().getCart().data?.toModel() ?: error("Gio hang khong hop le")
    }

    suspend fun getAddresses(): ResultState<List<Address>> = call("Khong lay duoc dia chi") {
        apiClient.service().getAddresses().data.orEmpty().map { it.toModel() }
    }

    suspend fun createAddress(
        fullName: String,
        phone: String,
        address: String,
    ): ResultState<Address> = call("Khong tao duoc dia chi giao hang") {
        apiClient.service().createAddress(
            CreateUserAddressRequest(
                receiverName = fullName.trim(),
                receiverPhone = phone.trim(),
                receiverAddress = address.trim(),
            ),
        ).data?.toModel() ?: error("Dia chi khong hop le")
    }

    suspend fun checkout(
        addressId: String,
        couponCode: String?,
        note: String?,
    ): ResultState<CheckoutResult> = call("Dat hang that bai") {
        val dto = apiClient.service().checkout(
            CheckoutRequest(
                addressId = addressId,
                couponCode = couponCode?.trim()?.ifBlank { null },
                note = note?.trim()?.ifBlank { null },
            ),
        ).data ?: error("Phan hoi dat hang khong hop le")
        CheckoutResult(
            orderId = dto.orderId,
            orderCode = dto.orderCode,
            paymentMethod = dto.paymentMethod,
            paymentStatus = dto.paymentStatus,
            totalAmount = dto.totalAmount,
            transferContent = dto.transferContent,
        )
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
