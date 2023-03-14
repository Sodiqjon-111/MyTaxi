package com.projects.mytaxi.dao

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "LOCATION")
@Parcelize
data class LocationInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 1,
    var lat: Double? = null,
    var lon: Double? = null,
) : Parcelable