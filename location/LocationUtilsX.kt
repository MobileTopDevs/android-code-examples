package com.solocator.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.location.altitude.AltitudeConverterCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocationUtilsX(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(
        minInterval: Long,
        quality: Int
    ): Flow<Location?> = callbackFlow {
        if (!LocationUtils.isLocationPermissionsGranted(context)) {
            Timber.e(Exception("Location permissions are missing"))
            trySend(null)

            awaitClose {  }
        } else {
            val locationListener = object : LocationListenerCompat {
                override fun onLocationChanged(location: Location) {
                    trySend(location)
                }

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}
            }

            val locationRequest =
                LocationRequestCompat.Builder(minInterval)
                    .setQuality(quality)
                    .build()

            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                LocationManager.GPS_PROVIDER,
                locationRequest,
                ContextCompat.getMainExecutor(context),
                locationListener
            )

            trySend(getLastKnownLocation())

            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        return if (!LocationUtils.isLocationPermissionsGranted(context)) {
            Timber.e(Exception("Location permissions are missing"))
            null
        } else {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }

    suspend fun addMslAltitudeToLocation(location: Location?): Location? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                location?.let {
                    AltitudeConverterCompat.addMslAltitudeToLocation(context, it)
                    it
                }
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    }
}
