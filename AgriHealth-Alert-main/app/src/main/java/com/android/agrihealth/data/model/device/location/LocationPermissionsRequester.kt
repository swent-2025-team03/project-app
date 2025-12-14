package com.android.agrihealth.data.model.device.location

import android.Manifest
import androidx.compose.runtime.Composable
import com.android.agrihealth.data.model.device.PermissionsRequester

/**
 * Creates a system Android pop up asking the user for the app to use their fine location
 *
 * @param onGranted Action to take if all location permissions have been granted
 * @param onDenied Action to take if any location permission has been denied
 * @param onComplete Action to take regardless of the user's choice
 */
@Composable
fun LocationPermissionsRequester(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
  val permissions =
      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  PermissionsRequester(permissions, onGranted, onDenied, onComplete)
}
