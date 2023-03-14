package com.projects.mytaxi.service

import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<android.location.Location>

    class LocationException(message: String) : Exception()

}