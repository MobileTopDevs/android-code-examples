package com.solocator.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    fun getLocationUpdates(minInterval: Long, quality: Int): Flow<Location?>

    suspend fun addMslAltitudeToLocation(location: Location?): Location?

    suspend fun getAddress(location: Location?, minDistance: Int): Pair<String, String>?

    fun getLockedLocation(): Flow<Location?>
}