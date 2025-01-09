package com.timelimiter.android.features.enter_pincode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.core.result.Result
import com.timelimiter.android.core.util.isActive
import com.timelimiter.android.domain.apps.ApprovePinCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterPinCodeViewModel @Inject constructor(
    private val approvePinCodeUseCase: ApprovePinCodeUseCase
) : ViewModel() {

    private var approvePinCodeJob: Job? = null
    private val _approvePinCodeResponse = MutableStateFlow<Result<Boolean>>(Result.Initial)
    val approvePinCodeResponse = _approvePinCodeResponse.asStateFlow()

    var inputCode by mutableStateOf("")
        private set

    fun updateInputCode(code: String) {
        this.inputCode = code
    }

    fun approvePinCode() {
        if (approvePinCodeJob.isActive) return
        approvePinCodeJob = viewModelScope.launch {
            approvePinCodeUseCase(inputCode).collect { _approvePinCodeResponse.emit(it) }
        }
    }
}