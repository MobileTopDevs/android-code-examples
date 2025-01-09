package com.timelimiter.android.features.wallpapers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpapersViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
): ViewModel() {

    val wpIndex = preferencesManager.getPreferencesFlow().map { it.wpIndex }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun setWallpaperIndex(index: Int) {
        viewModelScope.launch {
            preferencesManager.setWallpaperIndex(index)
        }
    }
}