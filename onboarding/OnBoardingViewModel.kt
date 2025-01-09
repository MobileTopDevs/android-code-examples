package com.timelimiter.android.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    fun finishedOnBoarding() {
        viewModelScope.launch {
            preferencesManager.setOnBoardingFinished()
        }
    }

    init {
        viewModelScope.launch { preferencesManager.setInstallDate(System.currentTimeMillis()) }
    }
}