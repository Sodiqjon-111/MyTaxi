package com.projects.mytaxi.dao

class LocationRepository(private val dataBase: AppDataBase) {
    suspend fun insertLocation(locationInfo: LocationInfo) =dataBase.loactionDao().insert(locationInfo)
}