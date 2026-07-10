package com.bookstore.mobile.feature.checkout.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.checkout.data.dto.CheckoutRequest
import com.bookstore.mobile.feature.profile.data.dto.CreateUserAddressRequest
import com.bookstore.mobile.shared.model.Address
import com.bookstore.mobile.shared.model.BestCouponSuggestion
import com.bookstore.mobile.shared.model.Cart
import com.bookstore.mobile.shared.model.CheckoutResult
import com.bookstore.mobile.feature.checkout.checkoutOrderStatusOrPending

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

    suspend fun getBestCoupon(
        cartItemIds: List<String>,
        shippingMethod: String,
    ): ResultState<BestCouponSuggestion> = call("Khong lay duoc goi y ma giam gia") {
        require(cartItemIds.isNotEmpty()) { "Can chon san pham de tim ma giam gia" }
        apiClient.service().getBestCoupon(cartItemIds, shippingMethod).data?.toModel()
            ?: error("Goi y ma giam gia khong hop le")
    }

    suspend fun checkout(request: CheckoutRequest): ResultState<CheckoutResult> = call("Dat hang that bai") {
        require(request.cartItemIds.isNotEmpty()) { "Can chon it nhat mot san pham de dat hang" }
        val dto = apiClient.service().checkout(
            request.copy(
                bookCouponCode = request.bookCouponCode?.trim()?.ifBlank { null },
                shippingCouponCode = request.shippingCouponCode?.trim()?.ifBlank { null },
                note = request.note?.trim()?.ifBlank { null },
            ),
        ).data ?: error("Phan hoi dat hang khong hop le")
        val orderStatus = checkoutOrderStatusOrPending(runCatching {
            apiClient.service().getOrder(dto.orderId).data?.status
        }.getOrNull())
        CheckoutResult(
            orderId = dto.orderId,
            orderCode = dto.orderCode,
            paymentMethod = dto.paymentMethod,
            paymentStatus = dto.paymentStatus,
            totalAmount = dto.totalAmount,
            transferContent = dto.transferContent,
            orderStatus = orderStatus,
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
