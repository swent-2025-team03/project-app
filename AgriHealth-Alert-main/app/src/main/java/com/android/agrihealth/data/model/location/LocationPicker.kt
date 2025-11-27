package com.android.agrihealth.data.model.location

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.ui.map.MapTopBar
import com.android.agrihealth.ui.map.MapViewModel
import com.android.agrihealth.ui.map.getUISettingsAndTheme
import com.android.agrihealth.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

object LocationPickerTestTags {
  const val MAP_SCREEN = "googleMapScreen"
  const val SELECT_LOCATION_BUTTON = "selectLocationButton"
  const val CONFIRMATION_PROMPT = "confirmationPromptBox"
  const val PROMPT_CONFIRM_BUTTON = "confirmationPromptYesButton"
  const val PROMPT_CANCEL_BUTTON = "confirmationPromptNoButton"
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
/**
 * Shows a map to choose a specific location with a pin.
 *
 * @param mapViewModel Map view model using a location repository
 * @param navigationActions Navigation Actions to navigate through the app
 * @param onLatLng Action to take once the user picks some coordinates
 * @param onAddress Action to take once the address is resolved
 */
fun LocationPicker(
    mapViewModel: MapViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
    onLatLng: (Double, Double) -> Unit = { _, _ -> },
    onAddress: (String?) -> Unit = {}
) {
  val context = LocalContext.current

  var showConfirmation by remember { mutableStateOf(false) }
  val uiState by mapViewModel.uiState.collectAsState()
  val address = uiState.geocodedAddress

  Scaffold(
      topBar = { MapTopBar(onBack = { navigationActions?.goBack() }, title = "Select a location") },
      bottomBar = {},
      content = { pd ->
        LocationPickerScreen(
            Modifier.padding(pd),
            mapViewModel,
            onLocationPicked = { lat, lng ->
              // TODO: maybe loading goes here
              onLatLng(lat, lng)
              showConfirmation = true
              mapViewModel.getAddressFromLatLng(context, lat, lng)
            })

        if (showConfirmation)
        // TODO: maybe loading here instead, if address is null
        AddressConfirmationPrompt(
                address,
                onConfirm = {
                  onAddress(address)
                  showConfirmation = false
                },
                onDismiss = { showConfirmation = false })
      })
}

@Composable
private fun LocationPickerScreen(
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

  Box(modifier = modifier.fillMaxSize()) {
    GoogleMap(
        modifier = Modifier.testTag(LocationPickerTestTags.MAP_SCREEN),
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
        modifier =
            Modifier.align(Alignment.BottomCenter)
                .padding(16.dp)
                .testTag(LocationPickerTestTags.SELECT_LOCATION_BUTTON)) {
          Text("Select this location")
        }
  }
}

@Composable
private fun AddressConfirmationPrompt(
    address: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  val text = if (address != null) "Detected address: $address" else "Finding address..."

  AlertDialog(
      modifier = Modifier.testTag(LocationPickerTestTags.CONFIRMATION_PROMPT),
      onDismissRequest = onDismiss,
      title = { Text("Confirm address") },
      containerColor = MaterialTheme.colorScheme.surface,
      text = { Text(text) },
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag(LocationPickerTestTags.PROMPT_CONFIRM_BUTTON),
            onClick = onConfirm) {
              Text("Confirm", color = MaterialTheme.colorScheme.onSurface)
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag(LocationPickerTestTags.PROMPT_CANCEL_BUTTON),
            onClick = onDismiss) {
              Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
      })
}
/*
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
*/
