package com.timelimiter.android.features.set_pincode

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetPinCodeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    var inputCode by mutableStateOf("")
        private set

    fun updateInputCode(code: String) {
        this.inputCode = code
    }

    fun saveCode() {
        viewModelScope.launch {
            preferencesManager.setPinCode(inputCode)
        }
    }
}