package com.timelimiter.android.features.add_general_app

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelimiter.android.core.result.Result
import com.timelimiter.android.core.util.isActive
import com.timelimiter.android.domain.apps.GetAppsSettingsUseCase
import com.timelimiter.android.domain.apps.GetAppsUseCase
import com.timelimiter.android.domain.apps.SaveAppsSettingsUseCase
import com.timelimiter.android.entities.apps.ItemApp
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
class AddGeneralAppViewModel @Inject constructor(
    getAppsSettingsUseCase: GetAppsSettingsUseCase,
    private val getAppsUseCase: GetAppsUseCase,
    private val saveAppsSettingsUseCase: SaveAppsSettingsUseCase,
) : ViewModel() {

    private val appsToSave: MutableSet<ItemApp> = mutableSetOf()

    private val _search = MutableStateFlow(TextFieldValue())
    val search = _search.asStateFlow()

    private var saveAppsJob: Job? = null
    private val _saveAppsResponse = MutableStateFlow<Result<Unit>>(Result.Initial)
    val saveAppsResponse = _saveAppsResponse.asStateFlow()

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
        combine(apps, settings, search) { apps, s, search ->

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

            // return result sorted by orderIndex and filtered by search value
            result
                ?.sortedBy { it.settings.orderIndex }
                ?.filter { it.label.contains(search.text, true) }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun getApps() {
        if (getAppsJob.isActive) return
        getAppsJob = viewModelScope.launch {
            getAppsUseCase().collect {
                _getAppsResponse.emit(it)
            }
        }
    }

    fun preSaveApp(app: ItemApp) {
        appsToSave.add(app)
    }

    fun updateSearchText(textFieldValue: TextFieldValue) {
        _search.value = textFieldValue
    }

    fun save() {
        if (saveAppsJob.isActive) return
        saveAppsJob = viewModelScope.launch {
            saveAppsSettingsUseCase(appsToSave.toList()).collect {
                _saveAppsResponse.emit(it)
            }
        }
    }
}