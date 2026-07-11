package com.bookstore.mobile.feature.order.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.order.data.dto.CancelOrderRequest
import com.bookstore.mobile.shared.model.Order
import com.bookstore.mobile.shared.model.OrderTimelineEvent

class OrderRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getOrders(): ResultState<List<Order>> = call("Khong lay duoc don hang") {
        apiClient.service().getOrders().data.orEmpty().map { it.toModel() }
    }

    suspend fun getOrder(id: String): ResultState<Order> = call("Khong lay duoc chi tiet don hang") {
        apiClient.service().getOrder(id).data?.toModel() ?: error("Khong tim thay don hang")
    }

    suspend fun getOrderTimeline(id: String): ResultState<List<OrderTimelineEvent>> =
        call("Khong lay duoc hanh trinh don hang") {
            apiClient.service().getOrderTimeline(id).data.orEmpty().map { it.toModel() }
        }

    suspend fun cancelOrder(id: String, reason: String): ResultState<Order> =
        call("Khong the huy don hang") {
            apiClient.service().cancelOrder(id, CancelOrderRequest(reason.trim())).data?.toModel()
                ?: error("Phan hoi huy don hang khong hop le")
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
