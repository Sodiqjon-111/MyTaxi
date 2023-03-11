package com.projects.mytaxi.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LOCATION")
data class LocationInfo(
    @PrimaryKey(autoGenerate = true)
    var id:Int=1,
    var lat:Double?=null,
    var lon:Double?=null,
)