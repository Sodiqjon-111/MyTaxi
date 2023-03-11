package com.projects.mytaxi.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocationInfo::class], version = 1, exportSchema = false)

abstract class AppDataBase : RoomDatabase() {
    abstract fun loactionDao(): LocationDao
}