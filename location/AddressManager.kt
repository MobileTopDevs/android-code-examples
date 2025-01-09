package com.solocator.util

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AddressManager(private val geocoder: Geocoder) {

    // save previous params until minDistance is achieved
    private var prevLocation: Location? = null
    private var prevAddress: Pair<String, String>? = null

    suspend fun getAddress(location: Location?, minDistance: Int): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                location ?: throw Exception("Location is null")

                val shouldUpdateAddress = prevAddress == null || prevLocation?.distanceTo(location)
                    ?.let { it >= minDistance } ?: true

                if (shouldUpdateAddress) {
                    prevLocation = location
                    prevAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        getAddressForApi33(location)
                    } else {
                        getAddressForLowerApi(location)
                    }
                }
                prevAddress
            } catch (e: Exception) {
                Timber.e(e)
                prevAddress = null
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getAddressForApi33(location: Location): Pair<String, String> {
        return suspendCancellableCoroutine { continuation ->
            val geocodeListener = object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: List<Address>) {
                    continuation.resume(extractAddressAndCity(addresses))
                }

                override fun onError(errorMessage: String?) {
                    continuation.resumeWithException(Exception(errorMessage))
                }
            }

            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                geocodeListener
            )
        }
    }

    private suspend fun getAddressForLowerApi(location: Location): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            extractAddressAndCity(addresses)
        }
    }

    private fun extractAddressAndCity(addresses: List<Address>?): Pair<String, String> {
        return addresses?.getOrNull(0)?.let { address ->

            val city = address.locality?.takeIf { it.isNotEmpty() }
                ?: throw Exception("No city found")

            val addressLine =
                address.getAddressLine(0)?.takeIf { it.isNotEmpty() } ?: buildString {
                    append(address.featureName.orEmpty())
                    address.subThoroughfare?.takeIf { it != address.featureName }?.let {
                        append("/$it")
                    }
                    append(" ")
                    address.thoroughfare?.let { append("$it, ") }
                    address.locality?.let { append("$it, ") }
                    address.adminArea?.let { append("$it, ") }
                    address.postalCode?.let { append(it) }
                }.takeIf { it.isNotBlank() } ?: throw Exception("No address found")

            addressLine to city
        } ?: throw Exception("Address list is empty")
    }
}