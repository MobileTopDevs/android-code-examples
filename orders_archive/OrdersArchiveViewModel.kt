package com.saasjohans.deliveryheroes.ui.orders_archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.models.order.Order
import com.saasjohans.deliveryheroes.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class OrdersArchiveViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _dateFilter = MutableStateFlow(
        savedStateHandle[DATE_FILTER] ?: LocalDateTime.now(ZoneOffset.UTC)
            .minusDays(1L)
    )

    val dateFilter = _dateFilter.asStateFlow()

    private var jobFetchOrders: Job? = null
    private val _ordersResponse = MutableStateFlow<Result<List<Order>>>(Result.Initial)
    val ordersResponse = _ordersResponse.asStateFlow()

    init {
        viewModelScope.launch {
            dateFilter.collect {
                savedStateHandle[DATE_FILTER] = it
                loadOrders(it)
            }
        }
    }

    private fun loadOrders(date: LocalDateTime) {
        jobFetchOrders?.cancel()
        jobFetchOrders = viewModelScope.launch {
            orderRepository.getArchiveOrders(date = date).collect {
                _ordersResponse.emit(it)
            }
        }
    }

    fun reloadOrders() {
        loadOrders(dateFilter.value)
    }

    fun setDate(localDateTime: LocalDateTime) {
        _dateFilter.value = localDateTime
    }

    fun resetOrdersResponse() {
        _ordersResponse.value = Result.Initial
    }

    companion object {
        private const val DATE_FILTER = "ORDERS_ARCHIVE_DATE_FILTER"
    }
}