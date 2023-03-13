package com.projects.mytaxi.dao

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch


class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val myDao: LocationDao
    private val myDataBase =
        Room.databaseBuilder(application, AppDataBase::class.java, "my_database").build()

    init {
        myDao = myDataBase.loactionDao()
    }

    fun insertLocation(locationInfo: LocationInfo) {
        viewModelScope.launch {
            myDao.insert(locationInfo)
        }
    }
}