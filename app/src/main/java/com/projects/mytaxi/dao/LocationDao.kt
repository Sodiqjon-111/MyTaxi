package com.projects.mytaxi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationInfo: LocationInfo)

    @Query("SELECT * FROM LOCATION")
    fun getLoactionInfo(): LocationInfo

}