package com.solocator.common

import android.content.SharedPreferences
import androidx.camera.core.ImageCapture
import com.solocator.camera.CameraSettings
import com.solocator.manager.OverlayPrefsStorage
import com.solocator.util.Constants
import com.solocator.util.GPSLocker
import com.solocator.util.booleanFlow
import com.solocator.util.floatFlow
import com.solocator.util.intFlow
import com.solocator.util.stringFlow
import com.solocator.util.stringSetFlow
import com.solocator.widget.TripleToggleButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import java.util.Locale

class SharedPrefsManager(private val sharedPreferences: SharedPreferences) {
    var isMetricUnits: Boolean
        get() = sharedPreferences.getBoolean(
            Constants.USE_METRIC_UNITS_KEY,
            Constants.USE_METRIC_UNITS_DEFAULT
        )
        set(value) = sharedPreferences.edit().putBoolean(
            Constants.USE_METRIC_UNITS_KEY,
            value
        ).apply()

    val isMetric = sharedPreferences.booleanFlow(
        Constants.USE_METRIC_UNITS_KEY,
        Constants.USE_METRIC_UNITS_DEFAULT
    )

    val saveToAlbumEnabled = sharedPreferences.booleanFlow(
        Constants.SAVE_TO_ALBUM_KEY,
        Constants.SAVE_TO_ALBUM_DEFAULT
    )

    val albumSuffixIndex = sharedPreferences.intFlow(
        Constants.SAVE_TO_ALBUM_SUFFIX_INDEX,
        Constants.SAVE_TO_ALBUM_SUFFIX_INDEX_DEFAULT
    )

    val cameraModeIndex = sharedPreferences.intFlow(
        Constants.CAMERA_MODE_KEY,
        Constants.CAMERA_MODE_DEFAULT_VALUE
    )

    val buildingSuffix = sharedPreferences.stringFlow(
        Constants.BUILDING_SUFFIX_KEY,
        Constants.BUILDING_SUFFIX_DEFAULT
    )

    val buildingSuffixSet = sharedPreferences.stringSetFlow(
        Constants.BUILDING_SUFFIX_SET_KEY,
        Constants.BUILDING_SUFFIX_SET_DEFAULT
    ).map {
        // for cases when user removed all suffixes
        // suffix list should never be empty
        if (it.isNullOrEmpty()) {
            Constants.BUILDING_SUFFIX_SET_DEFAULT
        } else {
            it
        }
    }

    val hasSensorAccelerometer = sharedPreferences.booleanFlow(
        Constants.FEATURE_SENSOR_ACCELEROMETER
    )

    val useInterCardinalDirections = sharedPreferences.booleanFlow(
        Constants.USE_INTER_CARDINAL_DIRECTIONS_KEY,
        Constants.USE_INTER_CARDINAL_DIRECTIONS_DEFAULT
    )

    val updateAddressEveryIndex = sharedPreferences.intFlow(
        Constants.UPDATING_DISTANCE_INDEX_SP,
        0
    )

    val showCaptureModes = sharedPreferences.booleanFlow(
        Constants.SHOW_CAPTURE_MODE_KEY,
        Constants.SHOW_CAPTURE_MODE_DEFAULT
    )

    val switchModesInCameraView = sharedPreferences.booleanFlow(
        Constants.SWITCH_CAMERA_MODES_KEY,
        Constants.SWITCH_CAMERA_MODES_DEFAULT
    )

    val isLandscapeLocked = sharedPreferences.booleanFlow(
        Constants.LANDSCAPE_LOCKED_KEY,
        Constants.LANDSCAPE_LOCKED_DEFAULT
    )

    val showGPSInfo = sharedPreferences.booleanFlow(
        Constants.SHOW_GPS_INFO_KEY,
        Constants.SHOW_GPS_INFO_DEFAULT
    )

    val isTiltRollEnabled = sharedPreferences.booleanFlow(
        OverlayPrefsStorage.TILT_ROLL_FLAG,
        false
    )

    val isTrueNorth = sharedPreferences.booleanFlow(
        Constants.USER_TRUE_NORTH_KEY,
        Constants.USER_TRUE_NORTH_DEFAULT
    )

    val projectName = sharedPreferences.stringFlow(
        Constants.CURRENT_PROJECT_SP,
        ""
    )

    val projectDescription = sharedPreferences.stringFlow(
        Constants.CURRENT_PROJECT_DESCRIPTION,
        ""
    )

    val projectNameAndDescriptionEnabled = sharedPreferences.booleanFlow(
        Constants.PROJECT_BTN_TURN
    )

    val isPlaceholderVisible = sharedPreferences.booleanFlow(
        Constants.SHOW_PLACEHOLDER_TEXT_KEY,
        Constants.SHOW_PLACEHOLDER_TEXT_DEFAULT
    )

    val plumbIndicatorEnabled = sharedPreferences.booleanFlow(
        Constants.SHOW_PLUMB_INDICATOR_KEY,
        Constants.SHOW_PLUMB_INDICATOR_DEFAULT
    )

    val isGpsLocked = sharedPreferences.booleanFlow(GPSLocker.SP_LOCK_KEY)
    val gpsLockedLat =
        sharedPreferences.stringFlow(GPSLocker.SP_LOCK_LAT).mapNotNull { it?.toDouble() }
    val gpsLockedLon =
        sharedPreferences.stringFlow(GPSLocker.SP_LOCK_LON).mapNotNull { it?.toDouble() }

    val isProjectBtnEnabled = sharedPreferences.booleanFlow(Constants.PROJECT_BTN_TURN)

    val showBearing = sharedPreferences.booleanFlow(
        Constants.SHOW_BEARING_KEY,
        Constants.SHOW_BEARING_DEFAULT
    )

    val showPosition = sharedPreferences.booleanFlow(
        Constants.SHOW_POSITION_KEY,
        Constants.SHOW_POSITION_DEFAULT
    )

    val showAccuracy = sharedPreferences.booleanFlow(
        Constants.SHOW_ACCURACY_KEY,
        Constants.SHOW_ACCURACY_DEFAULT
    )
    val showAltitude = sharedPreferences.booleanFlow(
        Constants.SHOW_ALTITUDE_KEY,
        Constants.SHOW_ALTITUDE_DEFAULT
    )
    val withIconKey = sharedPreferences.booleanFlow(
        Constants.GPS_INFO_WITH_ICON_KEY,
        Constants.GPS_INFO_WITH_ICON_DEFAULT
    )

    val showAltitudeMsl = sharedPreferences.booleanFlow(
        Constants.MSL_ALTITUDE_KEY,
        Constants.MSL_ALTITUDE_DEFAULT
    )

    val flashMode = sharedPreferences.intFlow(
        Constants.CAMERA_CHOSEN_FLASH_MODE_KEY,
        ImageCapture.FLASH_MODE_AUTO
    )

    val currentCameraIndex = sharedPreferences.intFlow(
        Constants.CAMERA_CHOSEN_INDEX_KEY,
        Constants.CAMERA_CHOSEN_INDEX_DEFAULT
    )

    val showLogoPlacementDialog = sharedPreferences.booleanFlow(
        Constants.SHOW_LOGO_PLACEMENT_DIALOG_KEY,
        true
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val cameraSettings = currentCameraIndex.flatMapLatest { cameraIndex ->
        sharedPreferences.intFlow(
            String.format(Locale.ENGLISH, Constants.CAMERA_CHOSEN_RESOLUTION_KEY, cameraIndex),
            Constants.CAMERA_CHOSEN_RESOLUTION_DEFAULT
        ).mapLatest { resolutionIndex ->
            CameraSettings(
                cameraIndex = cameraIndex,
                resolutionIndex = resolutionIndex
            )
        }
    }

    val rememberZoom = sharedPreferences.booleanFlow(Constants.CAMERA_REMEMBER_ZOOM_KEY)

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedZoomLevel = currentCameraIndex.flatMapLatest { cameraIndex ->
        sharedPreferences.floatFlow(
            String.format(Locale.ENGLISH, Constants.CAMERA_SAVED_ZOOM_LEVEL_KEY, cameraIndex),
            Constants.CAMERA_SAVED_ZOOM_LEVEL_DEFAULT
        )
    }

    val locationUpdateFrequency = sharedPreferences.intFlow(
        Constants.LOCATION_UPDATE_FREQUENCY_KEY,
        TripleToggleButton.RIGHT_BUTTON
    )

    val coordinateFormat = sharedPreferences.intFlow(Constants.COORDINATES_TYPE_KEY, 0)

    suspend fun saveIntPreferences(key: String, value: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putInt(key, value).apply()
        }
    }

    suspend fun saveFloatPreferences(key: String, value: Float) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putFloat(key, value).apply()
        }
    }

    suspend fun saveBoolPreferences(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }
    }

    suspend fun saveStringPreferences(key: String, value: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }

    suspend fun saveStringSetPreferences(key: String, value: Set<String>) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putStringSet(key, value).apply()
        }
    }
}