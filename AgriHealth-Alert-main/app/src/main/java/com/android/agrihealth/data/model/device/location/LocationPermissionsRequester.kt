package com.android.agrihealth.data.model.device.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun locationPermissionsRequester(locationViewModel: LocationViewModel = viewModel()): Boolean {
  val context = LocalContext.current
  val permissions =
      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  val granted = remember {
    mutableStateOf(
        permissions.all {
          ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
  }

  val locationPermissionRequest =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          results ->
        granted.value = results.values.all { it } // every permission granted
      }

  LaunchedEffect(Unit) {
    if (!granted.value) locationPermissionRequest.launch(permissions)
    else locationViewModel.getLastKnownLocation()
  }
  return granted.value
}

// TODO: Remove this block once location services are implemented somewhere else in the app
/*
@Composable
@Preview
fun LocationTestScreen() {
  Box {
    LocationRepositoryProvider.repository = LocationRepository(LocalContext.current)
    val viewModel = LocationViewModel()

    val location by viewModel.locationState.collectAsState()

    if (locationPermissionsRequester()) viewModel.getCurrentLocation()

    Column(Modifier.padding(16.dp).background(color = White)) {
      Text("Device Location")
      if (location != null) {
        Text("Lat: ${location!!.latitude}, Lng: ${location!!.longitude}")
      } else {
          if (viewModel.hasLocationPermissions()) Text("Fetching location...")
          else Text("Permission denied")
      }
    }
  }
}*/
