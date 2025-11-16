package com.android.agrihealth.data.model.device.location

import com.android.agrihealth.data.model.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocationProvider {
    val locationState: StateFlow<Location?>
    fun hasLocationPermissions(): Boolean
    fun getLastKnownLocation()
    fun getCurrentLocation()
}
