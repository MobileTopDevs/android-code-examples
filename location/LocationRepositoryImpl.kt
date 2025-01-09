package com.solocator.repository.impl

import android.location.Location
import com.solocator.repository.LocationRepository
import com.solocator.util.AddressManager
import com.solocator.util.LocationUtilsX
import com.solocator.util.toast.GPSLockerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationUtilsX: LocationUtilsX,
    private val addressManager: AddressManager,
    private val gpsLockerManager: GPSLockerManager
) : LocationRepository {
    override fun getLocationUpdates(minInterval: Long, quality: Int): Flow<Location?> {
        return locationUtilsX.getLocationUpdates(minInterval, quality)
    }

    override suspend fun addMslAltitudeToLocation(location: Location?): Location? {
        return locationUtilsX.addMslAltitudeToLocation(location)
    }

    override suspend fun getAddress(location: Location?, minDistance: Int): Pair<String, String>? {
        return addressManager.getAddress(location, minDistance)
    }

    override fun getLockedLocation(): Flow<Location?> = gpsLockerManager.getLockeLocation()

}