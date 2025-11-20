package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.device.location.LocationProvider
import com.android.agrihealth.data.model.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeLocationProvider(
    initialLocation: Location? = null,
    private val permissions: Boolean = true
) : LocationProvider {

  private val _state = MutableStateFlow(initialLocation)
  override val locationState: StateFlow<Location?> = _state

  override fun hasLocationPermissions(): Boolean = permissions

  override fun getLastKnownLocation() {
    // No-op: tests manually emit locations
  }

  override fun getCurrentLocation() {
    // No-op: tests manually emit locations
  }

  fun emitLocation(location: Location?) {
    _state.value = location
  }
}
