package com.timelimiter.android.features.games

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.core.util.isActive
import com.timelimiter.android.domain.apps.GetAppsSettingsUseCase
import com.timelimiter.android.domain.apps.GetAppsUseCase
import com.timelimiter.android.domain.apps.SaveAppsSettingsUseCase
import com.timelimiter.android.domain.apps.SetVisibleForGamesUseCase
import com.timelimiter.android.entities.apps.ItemApp
import com.timelimiter.android.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    getAppsSettingsUseCase: GetAppsSettingsUseCase,
    private val getAppsUseCase: GetAppsUseCase,
    private val saveAppsSettingsUseCase: SaveAppsSettingsUseCase,
    private val setVisibleForGamesUseCase: SetVisibleForGamesUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    var appProcessing by mutableStateOf<ItemApp?>(null)
        private set

    var initialStartTimeMinutes by mutableStateOf<Int?>(null)
    var initialEndTimeMinutes by mutableStateOf<Int?>(null)

    private var getAppsJob: Job? = null
    private val _getAppsResponse =
        MutableStateFlow<List<ItemApp>?>(null)
    private val apps = _getAppsResponse.asStateFlow()

    private val settings = getAppsSettingsUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    val appsWithSettings: StateFlow<List<ItemApp>?> =
        apps.combine(settings) { apps, s ->

            // Associate settings with apps
            val result = apps?.map { app ->
                val settings = s?.find { it.packageName == app.packageName }
                if (settings != null && app.settings != settings) {
                    app.copy(settings = settings)
                } else {
                    app
                }
            }

            //  Find apps without orderIndex and create settings for it
            if (result?.any { it.settings.orderIndex < 0 } == true) {
                viewModelScope.launch {
                    var maxIndex = result.maxBy { it.settings.orderIndex }.settings.orderIndex + 1
                    val appsWithoutIndex = result.filter { it.settings.orderIndex < 0 }
                    saveAppsSettingsUseCase(
                        appsWithoutIndex.map { app ->
                            app.copy(settings = app.settings.copy(orderIndex = maxIndex++))
                        }
                    ).collect()
                }
            }

            // return result sorted by orderIndex
            result
                ?.sortedBy { it.settings.orderIndex }
                ?.filter { it.settings.visibleForGames }
                ?.ifEmpty { null }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun getPickerTime() {
        viewModelScope.launch {
            preferencesManager.getPreferences().let { preferences ->
                initialStartTimeMinutes = preferences.gamesStartTimeMinutes
                initialEndTimeMinutes = preferences.gamesEndTimeMinutes
            }
        }
    }

    fun getApps() {
        if (getAppsJob.isActive) return
        getAppsJob = viewModelScope.launch {
            getAppsUseCase().collect {
                _getAppsResponse.emit(it)
            }
        }
    }

    fun setVisible(packageManager: String, flag: Boolean) {
        viewModelScope.launch {
            setVisibleForGamesUseCase(
                packageName = packageManager,
                flag = flag
            ).collect()
        }
    }

    fun processApp(app: ItemApp?) {
        appProcessing = app
    }

    fun setPickerStartTime(startTimeMinutes: Int) {
        viewModelScope.launch { preferencesManager.setGamesStartTimeMinutes(startTimeMinutes) }
    }

    fun setPickerEndTime(endTimeMinutes: Int) {
        viewModelScope.launch { preferencesManager.setGamesEndTimeMinutes(endTimeMinutes) }
    }
}