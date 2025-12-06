package com.android.agrihealth.data.model.device.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel() : ViewModel(), LocationProvider {
  private val locationRepository = LocationRepositoryProvider.repository

  private val _locationState = MutableStateFlow<Location?>(null)
  override val locationState = _locationState.asStateFlow()

  private val exceptionLogTag = "LocationServices"

  private fun securityExceptionLogMsg(e: SecurityException) =
      "Missing permissions for location: ${e.message}"

  private fun genericExceptionLogMsg(e: Exception) = "Something went wrong: ${e.message}"

  private fun throwIfNotAllowed() {
    check(hasLocationPermissions()) { "Location permissions not granted" }
  }

  private fun locationsEqual(l1: Location?, l2: Location?): Boolean {
    if (l1 == null || l2 == null) return false
    return (l1.latitude == l2.latitude) && (l1.longitude == l2.longitude)
  }

  /**
   * Gets the last known location, or, if unavailable, the current device location. Make sure the
   * user has given permissions first, for example using the "LocationPermissionsRequester"
   * composable
   */
  override fun getLastKnownLocation() {
    throwIfNotAllowed()
    viewModelScope.launch {
      try {
        val location = locationRepository.getLastKnownLocation()
        if (!locationsEqual(location, _locationState.value))
            _locationState.value = locationRepository.getLastKnownLocation()
      } catch (e: SecurityException) {
        Log.e(exceptionLogTag, securityExceptionLogMsg(e))
      } catch (e: Exception) {
        Log.e(exceptionLogTag, genericExceptionLogMsg(e))
      }
    }
  }

  /**
   * Gets the current device location. More expensive on the battery than last known location. Make
   * sure the user has given permissions first, for example using the "LocationPermissionsRequester"
   * composable
   */
  override fun getCurrentLocation() {
    throwIfNotAllowed()
    viewModelScope.launch {
      try {
        val location = locationRepository.getCurrentLocation()
        if (!locationsEqual(location, _locationState.value))
            _locationState.value = locationRepository.getCurrentLocation()
      } catch (e: SecurityException) {
        Log.e(exceptionLogTag, securityExceptionLogMsg(e))
      } catch (e: Exception) {
        Log.e(exceptionLogTag, genericExceptionLogMsg(e))
      }
    }
  }

  /** Checks if the user allowed all device location permissions on the app */
  override fun hasLocationPermissions(): Boolean {
    return locationRepository.hasFineLocationPermission() &&
        locationRepository.hasCoarseLocationPermission()
  }
}
