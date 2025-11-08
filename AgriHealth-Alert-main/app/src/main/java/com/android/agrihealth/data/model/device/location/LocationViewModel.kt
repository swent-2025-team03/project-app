package com.android.agrihealth.data.model.device.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel() : ViewModel() {
  private val locationRepository = LocationRepositoryProvider.repository

  private val _locationState = MutableStateFlow<Location?>(null)
  val locationState = _locationState.asStateFlow()

  private val exceptionLogTag = "LocationServices"

  private fun exceptionLogMsg(e: Exception) = "Missing permissions for location: ${e.message}"

  init {}

  /** Gets the last known location, or, if unavailable, the current device location */
  fun getLastKnownLocation() {
    viewModelScope.launch {
      try {
        _locationState.value = locationRepository.getLastKnownLocation()
      } catch (e: SecurityException) {
        Log.e(exceptionLogTag, exceptionLogMsg(e))
      }
    }
  }

  /** Gets the current device location. More expensive on the battery than last known location */
  fun getCurrentLocation() {
    viewModelScope.launch {
      try {
        _locationState.value = locationRepository.getCurrentLocation()
      } catch (e: SecurityException) {
        Log.e(exceptionLogTag, exceptionLogMsg(e))
      }
    }
  }
}
