package com.saasjohans.deliveryheroes.ui.home

import android.widget.CompoundButton
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saasjohans.deliveryheroes.data.Result
import com.saasjohans.deliveryheroes.data.local.manager.PreferencesManager
import com.saasjohans.deliveryheroes.data.models.OnBoardingState
import com.saasjohans.deliveryheroes.data.models.user.User
import com.saasjohans.deliveryheroes.data.repository.OrderRepository
import com.saasjohans.deliveryheroes.data.repository.UserRepository
import com.saasjohans.deliveryheroes.di.IODispatcher
import com.saasjohans.deliveryheroes.util.isActive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private var jobFetchOrders: Job? = null
    private val _ordersResponse = MutableStateFlow<Result<Unit>>(Result.Initial)
    val ordersResponse = _ordersResponse.asStateFlow()

    val orders = orderRepository.observeLocalOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private var jobFetchUser: Job? = null
    private val _userResponse = MutableStateFlow<Result<User>>(Result.Initial)
    val userResponse = _userResponse.asStateFlow()

    private var jobFetchChangeCourierStatus: Job? = null
    private val _changeCourierStatusResponse = MutableStateFlow<Result<Boolean>>(Result.Initial)
    val changeCourierStatusResponse = _changeCourierStatusResponse.asStateFlow()

    val user = userRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val availableForWork = userRepository.getUser().map { it.status }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val loading =
        combine(_ordersResponse, _userResponse, _changeCourierStatusResponse) { r1, r2, r3 ->
            r1.loading || r2.loading || r3.loading
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val onBoardingState = preferencesManager.getPreferencesFlow().map { it.onBoardingState }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun loadOrders() {
        jobFetchOrders?.cancel()
        jobFetchOrders = viewModelScope.launch {
            orderRepository.fetchOrdersFlow().collect {
                _ordersResponse.emit(it)
            }
        }
    }

    fun fetchUser() {
        jobFetchUser?.cancel()
        jobFetchUser = viewModelScope.launch {
            userRepository.refreshUser().collect {
                _userResponse.emit(it)
            }
        }
    }

    fun refreshAllData() {
        loadOrders()
        fetchUser()
    }

    fun onCourierStatusChange(switch: CompoundButton, checked: Boolean) {
        if (jobFetchChangeCourierStatus.isActive || !switch.isPressed) return
        jobFetchChangeCourierStatus = viewModelScope.launch {
            userRepository.changeCourierStatus().collect {
                _changeCourierStatusResponse.emit(it)
            }
        }
    }

    fun resetOrdersResponse() {
        _ordersResponse.value = Result.Initial
    }

    fun resetUserFetchResponse() {
        _userResponse.value = Result.Initial
    }

    fun resetChangeCourierStatusResponse() {
        _changeCourierStatusResponse.value = Result.Initial
    }

    fun setOnBoardingState(onBoardingState: OnBoardingState) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                preferencesManager.setOnBoardingState(onBoardingState)
            }
        }
    }
}