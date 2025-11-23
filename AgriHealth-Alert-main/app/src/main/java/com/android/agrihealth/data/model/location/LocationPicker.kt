package com.android.agrihealth.data.model.location

import android.location.LocationProvider
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.R
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerScreen(
  mapViewModel: MapViewModel = viewModel(),
  onLocationPicked: (LatLng) -> Unit = {},
  navigationActions: NavigationActions? = null
) {
  // Initial camera position
  val mapInitialLocation by mapViewModel.startingLocation.collectAsState()
  val mapInitialZoom by mapViewModel.zoom.collectAsState()
  val cameraPositionState = rememberCameraPositionState {}

  LaunchedEffect(mapInitialLocation) {
    cameraPositionState.position =
      CameraPosition.fromLatLngZoom(
        LatLng(mapInitialLocation.latitude, mapInitialLocation.longitude), mapInitialZoom
      )
  }

  // UI settings and theme
  val (googleMapUiSettings, googleMapMapProperties) = getUISettingsAndTheme()

  // Map
  Scaffold(
    topBar = { MapTopBar(onBack = { navigationActions?.goBack() }, title = "Select a location") },
    bottomBar = {},
    content = { pd ->
      Box(modifier = Modifier.fillMaxSize().padding(pd)) {
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
          modifier = Modifier
            .align(Alignment.Center)
            .offset(y = (-pinSize/2.0).dp) // pin tip points at the location
            .size(pinSize.dp)
        )

        // Confirm selection
        Button(
          onClick = {
            val pos = cameraPositionState.position.target
            onLocationPicked(pos)
          },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
        ) {
          Text("Select this location")
        }
      }
    }
  )

}

@Composable
@Preview
fun LocationPickerScreenPreview() {
  LocationRepositoryProvider.repository = LocationRepository(LocalContext.current)
  val mapViewModel = MapViewModel(locationViewModel = LocationViewModel(), showReports = false)
  AgriHealthAppTheme { LocationPickerScreen(mapViewModel = mapViewModel, onLocationPicked = { pos -> Log.d("LocationPicker", "Picked $pos")}) }
}
