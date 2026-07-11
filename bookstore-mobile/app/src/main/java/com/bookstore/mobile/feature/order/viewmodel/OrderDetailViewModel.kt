package com.bookstore.mobile.feature.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.order.data.OrderRepository
import com.bookstore.mobile.shared.model.Order
import com.bookstore.mobile.shared.model.OrderTimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val isTimelineLoading: Boolean = false,
    val order: Order? = null,
    val timeline: List<OrderTimelineEvent> = emptyList(),
    val errorMessage: String? = null,
    val timelineErrorMessage: String? = null,
    val isCancellingOrder: Boolean = false,
    val cancelErrorMessage: String? = null,
)

class OrderDetailViewModel(
    private val orderRepository: OrderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    fun load(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, timelineErrorMessage = null) }
            when (val result = orderRepository.getOrder(orderId)) {
                is ResultState.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isTimelineLoading = true,
                            order = result.data,
                            timeline = emptyList(),
                        )
                    }
                    loadTimeline(orderId)
                }

                is ResultState.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }

                else -> Unit
            }
        }
    }

    private suspend fun loadTimeline(orderId: String) {
        when (val result = orderRepository.getOrderTimeline(orderId)) {
            is ResultState.Success -> _uiState.update {
                it.copy(isTimelineLoading = false, timeline = result.data)
            }

            is ResultState.Error -> _uiState.update {
                it.copy(isTimelineLoading = false, timelineErrorMessage = result.message)
            }

            else -> _uiState.update { it.copy(isTimelineLoading = false) }
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        if (reason.trim().isEmpty() || _uiState.value.isCancellingOrder) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCancellingOrder = true, cancelErrorMessage = null) }
            when (val result = orderRepository.cancelOrder(orderId, reason)) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(order = result.data, isCancellingOrder = false) }
                    loadTimeline(orderId)
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isCancellingOrder = false, cancelErrorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isCancellingOrder = false) }
            }
        }
    }
}
