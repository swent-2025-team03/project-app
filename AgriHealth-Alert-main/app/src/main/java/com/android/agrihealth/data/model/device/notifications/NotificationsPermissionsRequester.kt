package com.android.agrihealth.data.model.device.notifications

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.android.agrihealth.data.model.device.PermissionsRequester

/**
 * Creates a system Android pop up asking the user for the app to send push notifications
 *
 * @param onGranted Action to take if notifications permission has been granted
 * @param onDenied Action to take if notifications permission was not granted
 * @param onComplete Action to take regardless of the user's choice
 */
@Composable
fun NotificationsPermissionsRequester(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
  // Android 13+ needs permissions for notifications
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

    PermissionsRequester(permissions, onGranted, onDenied, onComplete)
  }

  // ..but Android 12 and below automatically grants notifications permissions
  else {
    onGranted()
    onComplete()
  }
}
