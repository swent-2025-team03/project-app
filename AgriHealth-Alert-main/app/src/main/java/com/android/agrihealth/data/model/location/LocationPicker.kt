package com.android.agrihealth.data.model.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.ui.map.MapTopBar
import com.android.agrihealth.ui.map.MapViewModel
import com.android.agrihealth.ui.map.getUISettingsAndTheme
import com.android.agrihealth.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LocationPicker(
    mapViewModel: MapViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
    onAddress: (String?) -> Unit
) {
  val context = LocalContext.current

  Scaffold(
      topBar = { MapTopBar(onBack = { navigationActions?.goBack() }, title = "Select a location") },
      bottomBar = {},
      content = { pd ->
        LocationPickerScreen(
            Modifier.padding(pd),
            mapViewModel,
            onLocationPicked = { lat, lng ->
              getAddressFromLatLng(context, lat, lng, onResult = onAddress)
            })
      })
}

@Composable
fun LocationPickerScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(),
    onLocationPicked: (Double, Double) -> Unit,
) {
  // Initial camera position
  val mapInitialLocation by mapViewModel.startingLocation.collectAsState()
  val mapInitialZoom by mapViewModel.zoom.collectAsState()
  val cameraPositionState = rememberCameraPositionState {}

  LaunchedEffect(mapInitialLocation) {
    cameraPositionState.position =
        CameraPosition.fromLatLngZoom(
            LatLng(mapInitialLocation.latitude, mapInitialLocation.longitude), mapInitialZoom)
  }

  // UI settings and theme
  val (googleMapUiSettings, googleMapMapProperties) = getUISettingsAndTheme()

  Log.w("LocationPicker", "Picking a location")
  Box(modifier = modifier.fillMaxSize()) {
    GoogleMap(
        cameraPositionState = cameraPositionState,
        onMapLoaded = {},
        properties = googleMapMapProperties,
        uiSettings = googleMapUiSettings,
    )

    // Center pin
    val pinSize = 48
    Icon(
        imageVector = Icons.Filled.Place,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier =
            Modifier.align(Alignment.Center)
                .offset(y = (-pinSize / 2.0).dp) // pin tip points at the location
                .size(pinSize.dp))

    // Confirm selection
    Button(
        onClick = {
          val pos = cameraPositionState.position.target
          onLocationPicked(pos.latitude, pos.longitude)
        },
        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
          Text("Select this location")
        }
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun getAddressFromLatLng(context: Context, lat: Double, lng: Double, onResult: (String?) -> Unit) {
  val geocoder = Geocoder(context, Locale.getDefault())

  try {
    val addresses = geocoder.getFromLocation(lat, lng, 1)
    val result = addresses?.firstOrNull()?.getAddressLine(0)
    onResult(result)
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}

@Composable fun AddressConfirmationPrompt() {}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
@Preview
fun LocationPickerPreview() {
  val context = LocalContext.current

  LocationRepositoryProvider.repository = LocationRepository(context)
  val mapViewModel = MapViewModel(locationViewModel = LocationViewModel(), showReports = false)

  AgriHealthAppTheme {
    LocationPicker(
        mapViewModel, null, onAddress = { address -> Log.d("LocationPicker", "Address: $address") })
  }
}

@Composable
// @Preview
fun LocationPickerScreenPreview() {
  val context = LocalContext.current
  LocationRepositoryProvider.repository = LocationRepository(context)
  val mapViewModel = MapViewModel(locationViewModel = LocationViewModel(), showReports = false)
  AgriHealthAppTheme {
    LocationPickerScreen(
        mapViewModel = mapViewModel,
        onLocationPicked = { lat, lng ->
          Log.d("LocationPicker", "Selected coordinates Lat = $lat, Lng = $lng")
        })
  }
}
