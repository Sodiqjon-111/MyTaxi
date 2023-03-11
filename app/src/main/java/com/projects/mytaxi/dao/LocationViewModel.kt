package com.projects.mytaxi.dao

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: LocationRepository) :
    ViewModel() {
     suspend fun insertLocation(locationInfo: LocationInfo)=repository.insertLocation(locationInfo)
}