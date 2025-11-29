package com.android.agrihealth.data.model.device

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Creates a system Android pop up asking for the provided permissions for the app
 *
 * @param permissions Array(! not list) of permissions to request, format: Manifest.permission.X
 * @param onGranted Action to take if permissions are granted
 * @param onDenied Action to take if permissions are denied
 * @param onComplete Action to take regardless of the user's choice
 */
@Composable
fun PermissionsRequester(
    permissions: Array<String>,
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
  val context = LocalContext.current

  var granted by remember {
    mutableStateOf(
        permissions.all {
          ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
  }

  val permissionRequest =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          results ->
        granted = results.values.all { it } // every permission granted

        if (granted) onGranted() else onDenied()
        onComplete()
      }

  LaunchedEffect(Unit) { permissionRequest.launch(permissions) }
}
