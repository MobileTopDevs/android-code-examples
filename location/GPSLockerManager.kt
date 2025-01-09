package com.solocator.util.toast

import android.location.Location
import android.location.LocationManager
import com.solocator.common.SharedPrefsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GPSLockerManager(private val sharedPrefsManager: SharedPrefsManager) {

    fun getLockeLocation(): Flow<Location?> = combine(
        sharedPrefsManager.gpsLockedLat,
        sharedPrefsManager.gpsLockedLon
    ) { lat, lon ->
        Location(LocationManager.PASSIVE_PROVIDER).apply {
            latitude = lat
            longitude = lon
        }
    }
}