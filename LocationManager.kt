package com.zippe.client.features.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.zippe.client.core.util.isActive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class LocationManager(
    context: Context,
    private val scope: CoroutineScope
) : LocationCallback() {
    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    companion object {
        private const val TWO_MINUTES = 1000 * 60 * 2

        private const val LOCATION_UPDATE_TIME_INTERVAL_MS = 30_000L

        private fun getLocationRequest(): LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_UPDATE_TIME_INTERVAL_MS
        ).build()

        private fun areBothGpsProvidersEnabled(context: Context): Boolean {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        fun checkSystemLocationSettings(
            context: Context,
            locationEnableRequestResult: ActivityResultLauncher<IntentSenderRequest>
        ) {
            if (areBothGpsProvidersEnabled(context)) {
                return
            }
            val settingsBuilder = LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest()).apply {
                    setAlwaysShow(true)
                }

            LocationServices.getSettingsClient(context)
                .checkLocationSettings(settingsBuilder.build())
                .addOnCompleteListener { task ->
                    try {
                        task.getResult(ApiException::class.java)
                    } catch (ex: ApiException) {
                        when (ex.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                val resolvableApiException =
                                    ex as ResolvableApiException

                                locationEnableRequestResult.launch(
                                    IntentSenderRequest.Builder(
                                        resolvableApiException.resolution
                                    ).build()
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
        }
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocationJob: Job? = null

    fun start(withLastLocation: Boolean = true) {
        if (withLastLocation) {
            getLastLocation()
        }
        requestLocationUpdates()
    }

    fun stop() {
        lastLocationJob?.cancel()
        lastLocationJob = null
        removeLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                getLocationRequest(),
                this,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun removeLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        val newLocation = locationResult.lastLocation ?: return
        Timber.d("onLocationResult $newLocation")
        setLocation(newLocation)
    }

    private fun setLocation(newLocation: Location) {
        if (isBetterLocation(newLocation, _location.value)) {
            _location.value = newLocation
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (lastLocationJob.isActive) return
        lastLocationJob = scope.launch {
            try {
                val location = withContext(Dispatchers.IO) {
                    fusedLocationClient.lastLocation.await()
                } ?: return@launch
                if (_location.value == null) {
                    Timber.d("getLastLocation $location")
                    _lastKnownLocation.emit(location)
                }
            } catch (e: Exception) {
                Timber.e(e, "getLastLocation")
            }
        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     *
     * You might expect that the most recent location fix is the most accurate. However, because the accuracy
     * of a location fix varies, the most recent fix is not always the best.
     * You should include logic for choosing location fixes based on several criteria.
     * The criteria also varies depending on the use-cases of the application and field testing.
     *
     * Here are a few steps you can take to validate the accuracy of a location fix:
     *
     * Check if the location retrieved is significantly newer than the previous estimate.
     * Check if the accuracy claimed by the location is better or worse than the previous estimate.
     * Check which provider the new location is from and determine if you trust it more.
     *
     * An elaborate example of this logic can look something like this:
     */

    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            return true
        }
        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > TWO_MINUTES
        val isSignificantlyOlder = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        // Check if the old and new location are from the same provider
        val isFromSameProvider = isSameProvider(
            location.provider,
            currentBestLocation.provider
        )

        // Determine location quality using a combination of timeliness and accuracy
        return if (isMoreAccurate) {
            true
        } else if (isNewer && !isLessAccurate) {
            true
        } else
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider
    }

    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }
}