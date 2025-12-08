package com.android.agrihealth.ui.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.android.agrihealth.R
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

@Composable
fun getUISettingsAndTheme(): Pair<MapUiSettings, MapProperties> {
  val googleMapUiSettings = remember {
    MapUiSettings(
        zoomControlsEnabled = false,
    )
  }

  val context = LocalContext.current
  val darkTheme = isSystemInDarkTheme()
  val styleRes = if (darkTheme) R.raw.map_style_dark else R.raw.map_style_light
  val style = MapStyleOptions.loadRawResourceStyle(context, styleRes)

  val googleMapMapProperties = remember(style) { MapProperties(mapStyleOptions = style) }

  return Pair(googleMapUiSettings, googleMapMapProperties)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(onBack: () -> Unit, title: String = "Map") {
  TopAppBar(
      title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.weight(1f).testTag(MapScreenTestTags.TOP_BAR_MAP_TITLE))
            }
      },
      navigationIcon = {
        IconButton(
            onClick = onBack, modifier = Modifier.testTag(NavigationTestTags.GO_BACK_BUTTON)) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
      })
}
