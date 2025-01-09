package com.saasjohans.deliveryheroes.ui.order_accept

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.saasjohans.deliveryheroes.base.BaseViewModel
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderAcceptViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val orderId: Long =
        OrderAcceptFragmentArgs.fromSavedStateHandle(savedStateHandle).orderId

    val orderDetails = orderRepository.observeLocalOrderDetails(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private var jobAcceptOrder: Job? = null
    private val _acceptOrderResponse = MutableStateFlow<Result<Boolean>>(Result.Initial)
    val acceptOrderResponse = _acceptOrderResponse.asStateFlow()

    fun acceptOrder(accepted: Boolean) {
        jobAcceptOrder?.cancel()
        jobAcceptOrder = viewModelScope.launch {
            orderRepository.acceptOrderByCourier(
                orderId = orderId,
                accepted = accepted
            ).collect { result ->
                _acceptOrderResponse.emit(result)
            }
        }
    }

    fun resetOrderDetailsResponse() {
        _acceptOrderResponse.value = Result.Initial
    }
}