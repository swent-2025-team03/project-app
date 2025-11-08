package com.android.agrihealth.data.model.device.location

import android.Manifest
import android.content.Context
import android.location.Location as AndroidLocation
import androidx.annotation.RequiresPermission
import com.android.agrihealth.data.model.location.Location as AgrihealthLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationRepository(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
) {
  /**
   * Helper function to convert an Android device location to a location used in the rest of the
   * Agrihealth app
   */
  fun AndroidLocation.toLocation(): AgrihealthLocation {
    return AgrihealthLocation(latitude, longitude)
  }

  /** Gets the last known location, or, if unavailable, the current device location */
  @RequiresPermission(
      allOf =
          [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
  suspend fun getLastKnownLocation(): AgrihealthLocation {
    val lastLocation = fusedLocationClient.lastLocation.await()

    if (lastLocation != null) return lastLocation.toLocation()
    return getCurrentLocation()
  }

  /** Gets the current device location. More expensive on the battery than last known location */
  @RequiresPermission(
      allOf =
          [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
  suspend fun getCurrentLocation(): AgrihealthLocation {
    val currentLocation =
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .await()

    return currentLocation.toLocation()
  }
}
