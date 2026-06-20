package com.bookstore.mobile.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.checkout.data.CheckoutRepository
import com.bookstore.mobile.shared.model.Cart
import com.bookstore.mobile.shared.model.CheckoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val cart: Cart? = null,
    val fullName: String = "",
    val phone: String = "",
    val addressDetail: String = "",
    val provinceCity: String = "",
    val couponCode: String = "",
    val note: String = "",
    val errorMessage: String? = null,
    val checkoutResult: CheckoutResult? = null,
)

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cartResult = checkoutRepository.getCart()
            val addressResult = checkoutRepository.getAddresses()
            val firstAddress = (addressResult as? ResultState.Success)?.data
                ?.sortedByDescending { it.defaultAddress }
                ?.firstOrNull()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    cart = (cartResult as? ResultState.Success)?.data,
                    fullName = current.fullName.ifBlank { firstAddress?.receiverName.orEmpty() },
                    phone = current.phone.ifBlank { firstAddress?.receiverPhone.orEmpty() },
                    addressDetail = current.addressDetail.ifBlank { firstAddress?.receiverAddress.orEmpty() },
                    errorMessage = (cartResult as? ResultState.Error)?.message,
                )
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value, errorMessage = null) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }
    fun updateAddress(value: String) = _uiState.update { it.copy(addressDetail = value, errorMessage = null) }
    fun updateProvinceCity(value: String) = _uiState.update { it.copy(provinceCity = value, errorMessage = null) }
    fun updateCoupon(value: String) = _uiState.update { it.copy(couponCode = value, errorMessage = null) }
    fun updateNote(value: String) = _uiState.update { it.copy(note = value, errorMessage = null) }

    fun submit(onSuccess: (CheckoutResult) -> Unit) {
        val current = _uiState.value
        if (current.fullName.isBlank() || current.phone.isBlank() || current.addressDetail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nhap day du thong tin giao hang") }
            return
        }
        if (current.cart?.items.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "Gio hang dang trong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val mergedAddress = listOf(current.addressDetail, current.provinceCity)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            when (val addressResult = checkoutRepository.createAddress(current.fullName, current.phone, mergedAddress)) {
                is ResultState.Success -> {
                    when (val checkoutResult = checkoutRepository.checkout(
                        addressId = addressResult.data.id,
                        couponCode = current.couponCode,
                        note = current.note,
                    )) {
                        is ResultState.Success -> {
                            _uiState.update {
                                it.copy(isSubmitting = false, checkoutResult = checkoutResult.data)
                            }
                            onSuccess(checkoutResult.data)
                        }
                        is ResultState.Error -> _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = checkoutResult.message)
                        }
                        else -> Unit
                    }
                }
                is ResultState.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = addressResult.message)
                }
                else -> Unit
            }
        }
    }
}
