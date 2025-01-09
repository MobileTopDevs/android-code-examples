package com.timelimiter.android.features.apps

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.timelimiter.android.core.PREMIUM_VERSION
import com.timelimiter.android.core.TIME_FORMAT
import com.timelimiter.android.core.TRIAL_PERIOD_DAYS
import com.timelimiter.android.core.util.isActive
import com.timelimiter.android.domain.apps.GetAppsSettingsUseCase
import com.timelimiter.android.domain.apps.GetAppsUseCase
import com.timelimiter.android.domain.apps.GetProductDetailsUseCase
import com.timelimiter.android.domain.apps.GetProductStateUseCase
import com.timelimiter.android.domain.apps.GetTimeUseCase
import com.timelimiter.android.domain.apps.LaunchBillingFlowUseCase
import com.timelimiter.android.domain.apps.RefreshBillingInfoUseCase
import com.timelimiter.android.domain.apps.SaveAppsSettingsUseCase
import com.timelimiter.android.domain.apps.SwapIndexesUseCase
import com.timelimiter.android.entities.apps.ItemApp
import com.timelimiter.android.entities.apps.ItemAppSettings
import com.timelimiter.android.entities.apps.ItemsScreen
import com.timelimiter.android.entities.billing.ProductState
import com.timelimiter.android.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.joery.timerangepicker.TimeRangePicker
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    getAppsSettingsUseCase: GetAppsSettingsUseCase,
    private val getAppsUseCase: GetAppsUseCase,
    private val saveAppsSettingsUseCase: SaveAppsSettingsUseCase,
    private val swapIndexesUseCase: SwapIndexesUseCase,
    getTimeUseCase: GetTimeUseCase,
    getProductStateUseCase: GetProductStateUseCase,
    getProductDetailsUseCase: GetProductDetailsUseCase,
    private val launchBillingFlowUseCase: LaunchBillingFlowUseCase,
    private val refreshBillingInfoUseCase: RefreshBillingInfoUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private var getAppsJob: Job? = null
    private val _getAppsResponse =
        MutableStateFlow<List<ItemApp>?>(null)
    private val apps = _getAppsResponse.asStateFlow()

    private val productStateFlow = getProductStateUseCase(PREMIUM_VERSION)
        ?.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            ProductState.PRODUCT_STATE_UNKNOWN
        )

    private val productDetailsFlow = getProductDetailsUseCase(PREMIUM_VERSION)
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    var productDetails by mutableStateOf<ProductDetails?>(null)

    init {
        viewModelScope.launch {
            launch {
                productDetailsFlow?.collect {
                    Timber.d("$it")
                    productDetails = it
                }
            }
            launch {
                productStateFlow?.collect {
                    Timber.d("$it")
                    when (it) {
                        ProductState.PRODUCT_STATE_PURCHASED,
                        ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED -> {
                            setIsPaid(true)
                        }

                        ProductState.PRODUCT_STATE_UN_PURCHASED,
                        ProductState.PRODUCT_STATE_PENDING -> {
                            setIsPaid(false)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    var showPaidDialog by mutableStateOf(false)
        private set

    private val settings = getAppsSettingsUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    val isFirstLaunch = preferencesManager.getPreferencesFlow()
        .map { it.isFirstLaunch }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    private val time = getTimeUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        LocalTime.now()
    )

    val timeString = time.map {
        it.format(DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.getDefault()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val appsWithSettings: StateFlow<List<ItemApp>?> =
        combine(apps, settings) { apps, s ->

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

            result?.sortedBy { it.settings.orderIndex }  // sorting by order index
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val timedApps =
        combine(appsWithSettings, time, preferencesManager.getPreferencesFlow()) { apps, t, p ->
            apps?.filter { app ->  // filtering by time
                val generalAppsStartTime =
                    TimeRangePicker.Time(p.generalAppsStartTimeMinutes).localTime
                val generalAppsEndTime = TimeRangePicker.Time(p.generalAppsEndTimeMinutes).localTime
                val generalAppsStatement = t.isBetween(generalAppsStartTime, generalAppsEndTime)
                        && app.settings.visibleForGeneralApps

                val gamesStartTime = TimeRangePicker.Time(p.gamesStartTimeMinutes).localTime
                val gamesAppsEndTime = TimeRangePicker.Time(p.gamesEndTimeMinutes).localTime
                val gamesStatement = t.isBetween(gamesStartTime, gamesAppsEndTime)
                        && app.settings.visibleForGames

                generalAppsStatement || gamesStatement
            }
                ?.ifEmpty { null }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun getApps() {
        if (getAppsJob.isActive) return
        getAppsJob = viewModelScope.launch {
            getAppsUseCase().collect {
                _getAppsResponse.emit(it)
            }
        }
    }

    val screens = timedApps.map { apps ->
        apps?.chunked(size = ItemsScreen.ITEMS_ON_SCREEN)?.map { ItemsScreen(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun swapIndexes(settings1: ItemAppSettings, settings2: ItemAppSettings) {
        viewModelScope.launch {
            swapIndexesUseCase(settings1, settings2).collect()
        }
    }

    fun setFirstLaunch() {
        viewModelScope.launch {
            preferencesManager.setFirstLaunch(false)
        }
    }

    private suspend fun setIsPaid(flag: Boolean) {
        preferencesManager.setIsPaid(flag)
    }

    fun checkIsPaid() {
        viewModelScope.launch {
            val trialPeriod =
                TimeUnit.DAYS.toMillis(TRIAL_PERIOD_DAYS)
            val installDate = preferencesManager.getPreferences().installDate
            val isPaid = preferencesManager.getPreferences().isPaid
            showPaidDialog = !isPaid && trialPeriod < (System.currentTimeMillis() - installDate)
        }
    }

    fun closePaidDialog() {
        showPaidDialog = false
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        launchBillingFlowUseCase.invoke(activity, productDetails)
    }

    fun refreshBillingInfo() = refreshBillingInfoUseCase.invoke()
}

private fun LocalTime.isBetween(startTime: LocalTime, endTime: LocalTime): Boolean {
    return if (startTime.isAfter(endTime)) {
        !this.isBefore(startTime) || !this.isAfter(endTime)
    } else {
        !this.isBefore(startTime) && !this.isAfter(endTime)
    }
}