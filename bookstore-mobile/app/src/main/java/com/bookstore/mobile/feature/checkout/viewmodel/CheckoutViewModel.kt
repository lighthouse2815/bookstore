package com.bookstore.mobile.feature.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.checkout.CheckoutAddressPlan
import com.bookstore.mobile.feature.checkout.CheckoutCouponCodes
import com.bookstore.mobile.feature.checkout.CheckoutSubmissionDecision
import com.bookstore.mobile.feature.checkout.CheckoutIdempotencyKeyStore
import com.bookstore.mobile.feature.checkout.applyBestCouponSuggestion
import com.bookstore.mobile.feature.checkout.buildCheckoutSubmission
import com.bookstore.mobile.feature.checkout.data.CheckoutRepository
import com.bookstore.mobile.feature.checkout.selectedCheckoutItems
import com.bookstore.mobile.shared.model.Address
import com.bookstore.mobile.shared.model.BestCouponSuggestion
import com.bookstore.mobile.shared.model.Cart
import com.bookstore.mobile.shared.model.CartItem
import com.bookstore.mobile.shared.model.CheckoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SHIPPING_DELIVERY = "DELIVERY"
private const val SHIPPING_PICKUP = "PICKUP"
private const val PAYMENT_BANK_TRANSFER_QR = "BANK_TRANSFER_QR"
private const val PAYMENT_COD = "COD"

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isCouponLoading: Boolean = false,
    val cart: Cart? = null,
    val selectedItems: List<CartItem> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val isCreatingNewAddress: Boolean = false,
    val fullName: String = "",
    val phone: String = "",
    val addressDetail: String = "",
    val provinceCity: String = "",
    val shippingMethod: String = SHIPPING_DELIVERY,
    val paymentMethod: String = PAYMENT_BANK_TRANSFER_QR,
    val bookCouponCode: String = "",
    val shippingCouponCode: String = "",
    val bestCouponSuggestion: BestCouponSuggestion? = null,
    val couponMessage: String? = null,
    val note: String = "",
    val errorMessage: String? = null,
    val addressMessage: String? = null,
    val checkoutResult: CheckoutResult? = null,
) {
    val selectedTotalAmount: Double get() = selectedItems.sumOf { it.lineTotal }
}

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
) : ViewModel() {
    private val checkoutIdempotencyKeys = CheckoutIdempotencyKeyStore()
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun load(selectedCartItemIds: List<String>) {
        val requestedItemIds = selectedCartItemIds.distinct()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, addressMessage = null) }

            val cartResult = checkoutRepository.getCart()
            val addressResult = checkoutRepository.getAddresses()
            val cart = (cartResult as? ResultState.Success)?.data

            if (cart == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = (cartResult as? ResultState.Error)?.message
                            ?: "Khong lay duoc gio hang de thanh toan",
                    )
                }
                return@launch
            }

            val selectedItems = selectedCheckoutItems(cart.items, requestedItemIds)
            val addresses = (addressResult as? ResultState.Success)?.data.orEmpty()

            _uiState.update { current ->
                val retainedAddress = addresses.firstOrNull { it.id == current.selectedAddressId }
                val selectedAddress = retainedAddress
                    ?: addresses.firstOrNull { it.defaultAddress }
                    ?: addresses.firstOrNull()
                current.copy(
                    isLoading = false,
                    cart = cart,
                    selectedItems = selectedItems,
                    addresses = addresses,
                    selectedAddressId = selectedAddress?.id,
                    isCreatingNewAddress = selectedAddress == null,
                    errorMessage = when {
                        requestedItemIds.isEmpty() -> "Chon it nhat mot san pham de thanh toan"
                        selectedItems.isEmpty() -> "San pham da chon khong con trong gio hang"
                        else -> null
                    },
                    addressMessage = (addressResult as? ResultState.Error)?.message,
                )
            }

            if (selectedItems.isNotEmpty()) {
                refreshBestCoupon()
            }
        }
    }

    fun selectAddress(addressId: String) {
        if (_uiState.value.addresses.none { it.id == addressId }) return
        _uiState.update {
            it.copy(
                selectedAddressId = addressId,
                isCreatingNewAddress = false,
                errorMessage = null,
            )
        }
    }

    fun startCreatingAddress() {
        _uiState.update {
            it.copy(
                selectedAddressId = null,
                isCreatingNewAddress = true,
                errorMessage = null,
            )
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value) }

    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value) }

    fun updateAddress(value: String) = _uiState.update { it.copy(addressDetail = value) }

    fun updateProvinceCity(value: String) = _uiState.update { it.copy(provinceCity = value) }

    fun updateBookCoupon(value: String) = _uiState.update { it.copy(bookCouponCode = value) }

    fun updateShippingCoupon(value: String) = _uiState.update { it.copy(shippingCouponCode = value) }

    fun updateNote(value: String) = _uiState.update { it.copy(note = value) }

    fun selectShippingMethod(value: String) {
        if (value !in setOf(SHIPPING_DELIVERY, SHIPPING_PICKUP)) return
        _uiState.update { it.copy(shippingMethod = value, errorMessage = null) }
        refreshBestCoupon()
    }

    fun selectPaymentMethod(value: String) {
        if (value !in setOf(PAYMENT_BANK_TRANSFER_QR, PAYMENT_COD)) return
        _uiState.update { it.copy(paymentMethod = value, errorMessage = null) }
    }

    fun applySuggestedCoupon() {
        _uiState.update { current ->
            val couponCodes = applyBestCouponSuggestion(
                CheckoutCouponCodes(current.bookCouponCode, current.shippingCouponCode),
                current.bestCouponSuggestion,
            )
            current.copy(
                bookCouponCode = couponCodes.bookCouponCode,
                shippingCouponCode = couponCodes.shippingCouponCode,
            )
        }
    }

    fun refreshBestCoupon() {
        val current = _uiState.value
        val selectedItemIds = current.selectedItems.map { it.id }
        if (selectedItemIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCouponLoading = true, couponMessage = null) }
            when (
                val result = checkoutRepository.getBestCoupon(
                    cartItemIds = selectedItemIds,
                    shippingMethod = current.shippingMethod,
                )
            ) {
                is ResultState.Success -> _uiState.update {
                    it.copy(
                        isCouponLoading = false,
                        bestCouponSuggestion = result.data.takeIf { suggestion -> suggestion.available },
                        couponMessage = result.data.reason,
                    )
                }

                is ResultState.Error -> _uiState.update {
                    it.copy(
                        isCouponLoading = false,
                        bestCouponSuggestion = null,
                        couponMessage = result.message,
                    )
                }

                else -> _uiState.update { it.copy(isCouponLoading = false) }
            }
        }
    }

    fun submit(onSuccess: (CheckoutResult) -> Unit) {
        val current = _uiState.value
        val decision = buildCheckoutSubmission(
            isSubmitting = current.isSubmitting,
            selectedItems = current.selectedItems,
            selectedAddressId = current.selectedAddressId,
            isCreatingNewAddress = current.isCreatingNewAddress,
            fullName = current.fullName,
            phone = current.phone,
            addressDetail = current.addressDetail,
            provinceCity = current.provinceCity,
            shippingMethod = current.shippingMethod,
            paymentMethod = current.paymentMethod,
            bookCouponCode = current.bookCouponCode,
            shippingCouponCode = current.shippingCouponCode,
            note = current.note,
        )
        val readyDecision = decision as? CheckoutSubmissionDecision.Ready
        if (readyDecision == null) {
            val message = (decision as CheckoutSubmissionDecision.Rejected).message
            if (message != null) {
                _uiState.update { it.copy(errorMessage = message) }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val addressId = when (val addressPlan = readyDecision.addressPlan) {
                is CheckoutAddressPlan.New -> {
                    when (val result = checkoutRepository.createAddress(
                        addressPlan.fullName,
                        addressPlan.phone,
                        addressPlan.address,
                    )) {
                    is ResultState.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                selectedAddressId = result.data.id,
                                isCreatingNewAddress = false,
                                addresses = state.addresses + result.data,
                            )
                        }
                        result.data.id
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                        return@launch
                    }

                    else -> {
                        _uiState.update { it.copy(isSubmitting = false) }
                        return@launch
                    }
                }
                }

                is CheckoutAddressPlan.Existing -> addressPlan.addressId
            }

            val checkoutRequest = readyDecision.plan.toRequest(addressId)
            when (val result = checkoutRepository.checkout(
                checkoutRequest,
                checkoutIdempotencyKeys.keyFor(checkoutRequest),
            )) {
                is ResultState.Success -> {
                    checkoutIdempotencyKeys.clear()
                    _uiState.update { it.copy(isSubmitting = false, checkoutResult = result.data) }
                    onSuccess(result.data)
                }

                is ResultState.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }

                else -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
