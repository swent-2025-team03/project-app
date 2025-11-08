package com.android.agrihealth.data.model.device.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LocationPermissionsRequest(locationViewModel: LocationViewModel = viewModel()) {
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
}

@Composable
@Preview
fun LocationTestScreen() {
  Box {
    LocationRepositoryProvider.repository = LocationRepository(LocalContext.current)
    val viewModel = LocationViewModel()

    val location by viewModel.locationState.collectAsState()

    LocationPermissionsRequest()
    viewModel.getCurrentLocation()

    Column(Modifier.padding(16.dp).background(color = White)) {
      Text("Device Location")
      if (location != null) {
        Text("Lat: ${location!!.latitude}, Lng: ${location!!.longitude}")
      } else {
        Text("Fetching location...")
      }
    }
  }
}
