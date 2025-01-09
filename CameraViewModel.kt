package com.solocator.camera

import android.location.Location
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.core.location.LocationCompat
import androidx.core.location.LocationRequestCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solocator.cameraUtils.CameraMode
import com.solocator.cameraUtils.DisplayOrientation
import com.solocator.common.OverlayPrefsManager
import com.solocator.common.SharedPrefsManager
import com.solocator.model.camera.OrientationAngles
import com.solocator.model.camera.OverlayLocationSettings
import com.solocator.model.camera.OverlaySettings
import com.solocator.model.settings.CaptureModeSettings
import com.solocator.repository.LocationRepository
import com.solocator.util.Constants
import com.solocator.util.LocationController
import com.solocator.util.Utils
import com.solocator.util.camera.CameraHelper
import com.solocator.widget.TripleToggleButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    orientationManager: OrientationManager,
    private val locationRepository: LocationRepository,
    private val sharedPrefsManager: SharedPrefsManager,
    overlayPrefsManager: OverlayPrefsManager,
    private val cameraHelper: CameraHelper
) : ViewModel() {

    private val _providerAvailable = MutableStateFlow(false)
    val providerAvailable = _providerAvailable.asStateFlow()

    private val lensFacing = MutableStateFlow(LENS_FACING_BACK)

    private val _isTrialOrBoughtVersion = MutableStateFlow(false)
    val isTrialOrBoughtVersion = _isTrialOrBoughtVersion.asStateFlow()
    val isFreeVersion = _isTrialOrBoughtVersion.map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    private var getLocationJob: Job? = null
    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    val locationWithMsl = location.map {
        locationRepository.addMslAltitudeToLocation(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val address = location.combine(sharedPrefsManager.updateAddressEveryIndex) { loc, updateIndex ->
        locationRepository.getAddress(
            location = loc,
            minDistance = LocationController.distance_metrics[updateIndex]
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val orientationAngles = orientationManager.orientationAnglesFlow
        .map { angles ->
            OrientationAngles(
                azimuth = angles[0],
                tilt = angles[1],
                roll = angles[2]
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            OrientationAngles()
        )

    val orientationAnglesArray = orientationManager.orientationAnglesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            floatArrayOf(0f, 0f, 0f)
        )

    val bearing = combine(
        sharedPrefsManager.isTrueNorth,
        location,
        orientationAngles,
        lensFacing
    ) { isTrueNorth, location, angles, lensFacing ->
        var result = angles.azimuth
        if (lensFacing != LENS_FACING_BACK) result += 180f
        result = (result + 360) % 360

        if (isTrueNorth) {
            Utils.calculateTrueNorth(location, result)
        } else {
            result
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    private val _cameraRatio = MutableStateFlow<String?>(null)
    val cameraRatio = _cameraRatio.asStateFlow()

    val saveToAlbumEnabled = sharedPrefsManager.saveToAlbumEnabled
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val albumSuffixIndex = sharedPrefsManager.albumSuffixIndex
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val showBearing = MutableStateFlow(true)
    val showPosition = MutableStateFlow(true)
    val showAltitude = MutableStateFlow(true)
    val showGPSInfo = MutableStateFlow(true)

    val showAltitudeMsl = sharedPrefsManager.showAltitudeMsl
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val showAccuracy = sharedPrefsManager.showAccuracy
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val flashMode = sharedPrefsManager.flashMode
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FLASH_MODE_AUTO)

    val cameraSettings = sharedPrefsManager.cameraSettings
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val rememberZoom = sharedPrefsManager.rememberZoom
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val savedZoomLevel = sharedPrefsManager.savedZoomLevel
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            Constants.CAMERA_SAVED_ZOOM_LEVEL_DEFAULT
        )

    val captureModeSettings = combine(
        sharedPrefsManager.cameraModeIndex,
        sharedPrefsManager.buildingSuffix,
        sharedPrefsManager.useInterCardinalDirections,
        sharedPrefsManager.updateAddressEveryIndex,
        sharedPrefsManager.showCaptureModes,
        sharedPrefsManager.switchModesInCameraView
    ) { settings ->
        CaptureModeSettings(
            cameraMode = settings[0] as Int,
            buildingSuffix = settings[1] as String,
            useInterCardinalDirections = settings[2] as Boolean,
            updateAddressEvery = settings[3] as Int,
            showCaptureModes = settings[4] as Boolean,
            switchModesInCameraView = settings[5] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CaptureModeSettings())

    val cameraMode = captureModeSettings.map {
        CameraMode.of(it.cameraMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CameraMode.COMPASS)

    val displayOrientation = orientationManager.fixedDisplayOrientation
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            DisplayOrientation.PORTRAIT_NORMAL
        )

    val locationSettings = combine(
        sharedPrefsManager.isMetric, // 0
        sharedPrefsManager.withIconKey, // 1
        sharedPrefsManager.isTrueNorth, // 2
        sharedPrefsManager.showBearing, // 3
        sharedPrefsManager.showPosition, // 4
        sharedPrefsManager.showAccuracy, // 5
        sharedPrefsManager.showAltitude, // 6
        sharedPrefsManager.coordinateFormat, // 7
        bearing, // 8
        location, // 9
        showAltitudeMsl, // 10
        locationWithMsl // 11
    ) { settings ->
        val location = settings[9] as Location?

        val locationWithMsl = settings[11] as Location?
        val mslAltitude = locationWithMsl
            ?.takeIf { LocationCompat.hasMslAltitude(it) }
            ?.let { LocationCompat.getMslAltitudeMeters(it) }

        OverlayLocationSettings(
            isMetric = settings[0] as Boolean,
            isGpsEnabled = LocationController.isGpsEnabled(),
            withIconKey = settings[1] as Boolean,
            useTrueNorth = settings[2] as Boolean,
            showBearing = settings[3] as Boolean,
            showPosition = settings[4] as Boolean,
            showAccuracy = settings[5] as Boolean,
            showAltitude = settings[6] as Boolean,
            format = settings[7] as Int,
            bearing = settings[8] as Float,
            latitude = location?.latitude,
            longitude = location?.longitude,
            accuracy = location?.accuracy,
            altitude = location?.altitude,
            showMslAltitude = settings[10] as Boolean,
            mslAltitude = mslAltitude
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OverlayLocationSettings())

    val overlaySettings = overlayPrefsManager.overlaySettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OverlaySettings())

    private val _underBarText = MutableStateFlow("")
    val underBarText = _underBarText.asStateFlow()

    private val _dateText = MutableStateFlow("")
    val dateText = _dateText.asStateFlow()

    val isPlaceholderVisible = sharedPrefsManager.isPlaceholderVisible
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val plumbIndicatorEnabled = sharedPrefsManager.plumbIndicatorEnabled
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val isGpsLocked = sharedPrefsManager.isGpsLocked
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val photosProcessing = MutableStateFlow(0)
    val currentZoomValue = MutableStateFlow<Float?>(null)

    init {

        viewModelScope.launch {
            sharedPrefsManager.locationUpdateFrequency.combine(isGpsLocked) { freq, locked ->
                if (locked) {
                    getLockedLocation()
                } else {
                    val interval = when (freq) {
                        TripleToggleButton.LEFT_BUTTON -> 10.times(1000L)
                        TripleToggleButton.CENTER_BUTTON -> 5.times(1000L)
                        else -> 0
                    }
                    getCurrentLocation(interval)
                }
            }.collect()
        }
        viewModelScope.launch {
            launch {
                sharedPrefsManager.showGPSInfo.collect { saved -> showGPSInfo.update { saved } }
            }
            launch {
                sharedPrefsManager.showBearing.collect { saved -> showBearing.update { saved } }
            }
            launch {
                sharedPrefsManager.showPosition.collect { saved -> showPosition.update { saved } }
            }
            launch {
                sharedPrefsManager.showAltitude.collect { saved -> showAltitude.update { saved } }
            }
        }
    }

    private fun getCurrentLocation(
        interval: Long,
        quality: Int = LocationRequestCompat.QUALITY_HIGH_ACCURACY
    ) {
        getLocationJob?.cancel()
        getLocationJob = viewModelScope.launch {
            sharedPrefsManager.isGpsLocked
            locationRepository.getLocationUpdates(interval, quality).collect {
                _location.emit(it)
            }
        }
    }

    private fun getLockedLocation() {
        getLocationJob?.cancel()
        getLocationJob = viewModelScope.launch {
            locationRepository.getLockedLocation().collect {
                _location.emit(it)
            }
        }
    }

    val onFlashModeSelected = { mode: Int ->
        viewModelScope.launch(Dispatchers.IO) {
            sharedPrefsManager.saveIntPreferences(
                Constants.CAMERA_CHOSEN_FLASH_MODE_KEY,
                mode
            )
        }
        Unit
    }

    fun toggleFlashMode() {
        when (flashMode.value) {
            FLASH_MODE_AUTO -> onFlashModeSelected.invoke(FLASH_MODE_ON)
            FLASH_MODE_ON -> onFlashModeSelected.invoke(FLASH_MODE_OFF)
            else -> onFlashModeSelected.invoke(FLASH_MODE_AUTO)
        }
    }

    fun switchCamera() {
        val newIndex: Int = cameraSettings.value?.cameraIndex?.let {
            (it + 1) % cameraHelper.getCameraCount()
        } ?: 0
        saveCurrentCameraIndex(newIndex)
    }

    fun addPhotoProcessing() {
        photosProcessing.value++
    }

    fun removePhotoProcessing() {
        photosProcessing.value--
    }

    fun saveCameraMode(index: Int) {
        viewModelScope.launch {
            sharedPrefsManager.saveIntPreferences(
                Constants.CAMERA_MODE_KEY,
                index
            )
        }
    }

    fun saveUseInterCardinalDirections(enabled: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.USE_INTER_CARDINAL_DIRECTIONS_KEY,
                enabled
            )
        }
    }

    fun saveAddressEveryIndex(index: Int) {
        viewModelScope.launch {
            sharedPrefsManager.saveIntPreferences(
                Constants.UPDATING_DISTANCE_INDEX_SP,
                index
            )
        }
    }

    fun saveCurrentCameraIndex(index: Int) {
        viewModelScope.launch {
            sharedPrefsManager.saveIntPreferences(
                Constants.CAMERA_CHOSEN_INDEX_KEY,
                index
            )
        }
    }

    fun saveCurrentResolutionIndex(cameraIndex: Int, index: Int) {
        viewModelScope.launch {
            sharedPrefsManager.saveIntPreferences(
                String.format(Locale.ENGLISH, Constants.CAMERA_CHOSEN_RESOLUTION_KEY, cameraIndex),
                index
            )
        }
    }

    fun saveRememberZoomLevel(enabled: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.CAMERA_REMEMBER_ZOOM_KEY,
                enabled
            )
        }
    }

    fun saveZoomLevel(zoom: Float) {
        val cameraIndex = cameraSettings.value?.cameraIndex ?: return
        if (!rememberZoom.value) return
        viewModelScope.launch {
            sharedPrefsManager.saveFloatPreferences(
                String.format(Locale.ENGLISH, Constants.CAMERA_SAVED_ZOOM_LEVEL_KEY, cameraIndex),
                zoom
            )
        }
    }

    fun saveShowCaptureModes(enabled: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_CAPTURE_MODE_KEY,
                enabled
            )
        }
    }

    fun saveSwitchModesInCameraView(enabled: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SWITCH_CAMERA_MODES_KEY,
                enabled
            )
        }
    }

    fun updateCameraRatio(ratio: String) {
        _cameraRatio.value = ratio
    }

    fun updateUnderBarText(text: String) {
        _underBarText.value = text
    }

    fun updateDateText(text: String) {
        _dateText.value = text
    }

    fun updateVersion(flag: Boolean) {
        _isTrialOrBoughtVersion.value = flag
    }

    fun setProviderAvailable(flag: Boolean) {
        _providerAvailable.value = flag
    }

    fun updateLensFacing(flag: Int) {
        lensFacing.value = flag
    }

    fun updateCurrentZoom(value: Float) {
        currentZoomValue.value = value
    }

    fun saveShowGpsInfo(checked: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_GPS_INFO_KEY,
                checked
            )
        }
        val options = listOf(
            showBearing.value,
            showPosition.value,
            showAltitude.value
        )
        if (checked && options.all { !it }) {
            saveShowBearing(true)
            saveShowPosition(true)
            saveShowAltitude(true)
        }
    }

    fun saveShowBearing(checked: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_BEARING_KEY,
                checked
            )
        }
        val options = listOf(
            checked,
            showPosition.value,
            showAltitude.value
        )
        if (options.all { !it }) {
            saveShowGpsInfo(false)
        }
    }

    fun saveShowPosition(checked: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_POSITION_KEY,
                checked
            )
        }
        val options = listOf(
            showBearing.value,
            checked,
            showAltitude.value
        )
        if (options.all { !it }) {
            saveShowGpsInfo(false)
        }
    }

    fun saveShowAltitude(checked: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_ALTITUDE_KEY,
                checked
            )
        }
        val options = listOf(
            showBearing.value,
            showPosition.value,
            checked
        )
        if (options.all { !it }) {
            saveShowGpsInfo(false)
        }
    }

    fun saveShowAccuracy(checked: Boolean) {
        viewModelScope.launch {
            sharedPrefsManager.saveBoolPreferences(
                Constants.SHOW_ACCURACY_KEY,
                checked
            )
        }
    }
}