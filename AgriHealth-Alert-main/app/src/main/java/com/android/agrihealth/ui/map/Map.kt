package com.android.agrihealth.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.R
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.core.design.theme.statusColor
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import com.android.agrihealth.ui.user.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "googleMapScreen"
  const val TOP_BAR_MAP_TITLE = "topBarMapTitle"
  const val REPORT_INFO_BOX = "reportInfoBox"
  const val REPORT_FILTER_MENU = "reportFilterDropdownMenu"
  const val REPORT_NAVIGATION_BUTTON = "reportNavigationButton"
  const val REFRESH_BUTTON = "mapRefreshButton"

  // from bootcamp map
  fun getTestTagForReportMarker(reportId: String): String = "reportMarker_$reportId"

  fun getTestTagForReportTitle(reportId: String): String = "reportTitle_$reportId"

  fun getTestTagForReportDesc(reportId: String): String = "reportDescription_$reportId"

  fun getTestTagForFilter(filter: String): String = "filter_$filter"
}

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
    isViewedFromOverview: Boolean = true,
    startingPosition: Location? = null
) {
  val uiState by mapViewModel.uiState.collectAsState()
  val user by userViewModel.user.collectAsState()
  var selectedFilter by remember { mutableStateOf("All") }

  val mapInitialLocation by mapViewModel.startingLocation.collectAsState()
  val mapInitialZoom by mapViewModel.zoom.collectAsState()
  val cameraPositionState = rememberCameraPositionState {}

  mapViewModel.setStartingLocation(startingPosition)

  LaunchedEffect(mapInitialLocation) {
    cameraPositionState.position =
        CameraPosition.fromLatLngZoom(
            LatLng(mapInitialLocation.latitude, mapInitialLocation.longitude), mapInitialZoom)
  }

  val selectedReport = remember { mutableStateOf(mapViewModel.selectedReport) }
  mapViewModel.refreshReports(user.uid)

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

  Scaffold(
      topBar = { if (!isViewedFromOverview) MapTopBar(onBack = { navigationActions?.goBack() }) },
      bottomBar = {
        if (isViewedFromOverview)
            BottomNavigationMenu(
                selectedTab = Tab.Map,
                onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
                modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd ->
        Box(modifier = Modifier.fillMaxSize().padding(pd)) {
          GoogleMap(
              cameraPositionState = cameraPositionState,
              properties = googleMapMapProperties,
              uiSettings = googleMapUiSettings,
              modifier = Modifier.testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN)) {
                mapViewModel
                    .spiderifiedReports()
                    .filter { it ->
                      selectedFilter == "All" || it.first.status.displayString() == selectedFilter
                    }
                    .forEach { it ->
                      val report = it.first
                      val markerSize = if (report == selectedReport.value) 60f else 40f
                      val markerIcon =
                          createCircleMarker(statusColor(report.status).toArgb(), markerSize)
                      Marker(
                          state = MarkerState(position = it.second),
                          title = report.title,
                          snippet = report.description,
                          icon = markerIcon,
                          onClick = {
                            selectedReport.value =
                                if (selectedReport.value == report) null else report
                            true
                          },
                      /*tag = testTag*/ )
                    }
              }

          // Debug box to make tests work
          // Because Google map markers aren't accessible in compose tests, so I have to make this
          // item
          // If the box is empty or has size 0, the composable doesn't exist and tests fail
          // Yes, this sucks
          uiState.reports
              .filter { report ->
                selectedFilter == "All" || report.status.displayString() == selectedFilter
              }
              .forEach { report ->
                Log.d("MapScreen", "Creating debug box ${report.id}")
                Box(
                    modifier =
                        Modifier.testTag(MapScreenTestTags.getTestTagForReportMarker(report.id))
                            .clickable {
                              selectedReport.value =
                                  if (selectedReport.value == report) null else report
                            }
                            .alpha(0f)
                            .size(1.dp)) {
                      Text(":)")
                    }
              }

          if (isViewedFromOverview) {
            val options = listOf("All") + ReportStatus.entries.map { it.displayString() }
            FilterDropdown(options, selectedFilter) { selectedFilter = it }
          }

          FloatingActionButton(
              modifier =
                  Modifier.align(Alignment.BottomEnd)
                      .padding(16.dp)
                      .testTag(MapScreenTestTags.REFRESH_BUTTON),
              onClick = {
                mapViewModel.refreshCameraPosition()

                val newPos = mapViewModel.startingLocation.value
                val newLat = newPos.latitude
                val newLng = newPos.longitude
                val newZoom = mapViewModel.zoom.value

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(LatLng(newLat, newLng), newZoom))
              }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Location")
              }

          ShowReportInfo(
              selectedReport.value,
              onReportClick = { navigationActions?.navigateTo(Screen.ViewReport(it)) })
        }
      })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(onBack: () -> Unit) {
  TopAppBar(
      title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "Map",
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

@Composable
fun FilterDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(
      modifier =
          Modifier.padding(16.dp)
              .clickable { expanded = true }
              .defaultMinSize(minWidth = 100.dp, minHeight = 40.dp)
              .background(color = MaterialTheme.colorScheme.surface)
              .border(2.dp, MaterialTheme.colorScheme.primary)
              .testTag(MapScreenTestTags.REPORT_FILTER_MENU),
      contentAlignment = Alignment.Center) {
        Text(selectedOption)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          options.forEach { option ->
            DropdownMenuItem(
                onClick = {
                  onOptionSelected(option)
                  expanded = false
                },
                text = { Text(option) },
                modifier = Modifier.testTag(MapScreenTestTags.getTestTagForFilter(option)))
          }
        }
      }
}

@Composable
fun ShowReportInfo(report: Report?, onReportClick: (String) -> Unit = {}) {
  if (report == null) return

  Box(modifier = Modifier.fillMaxSize().testTag(MapScreenTestTags.REPORT_INFO_BOX)) {
    Column(
        modifier =
            Modifier.background(color = MaterialTheme.colorScheme.surface)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.testTag(MapScreenTestTags.getTestTagForReportTitle(report.id)))

                Button(
                    onClick = { onReportClick(report.id) },
                    modifier =
                        Modifier.align(Alignment.CenterVertically)
                            .testTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON)) {
                      Text("See report")
                    }
              }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = report.description,
              modifier = Modifier.testTag(MapScreenTestTags.getTestTagForReportDesc(report.id)))
          Spacer(modifier = Modifier.height(8.dp))
        }
  }
}

fun createCircleMarker(color: Int, radius: Float = 40f, strokeWidth: Float = 8f): BitmapDescriptor {
  val size = (radius * 2 + strokeWidth).toInt()
  val bitmap = createBitmap(size, size)
  val canvas = Canvas(bitmap)

  // Draw filled circle
  val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  paint.color = color
  paint.style = Paint.Style.FILL
  canvas.drawCircle(size / 2f, size / 2f, radius, paint)

  // Draw white outline
  val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  strokePaint.color = Color.WHITE
  strokePaint.style = Paint.Style.STROKE
  strokePaint.strokeWidth = strokeWidth
  canvas.drawCircle(size / 2f, size / 2f, radius, strokePaint)

  // Draw white center dot
  val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  val dotRadius = radius / 4f
  dotPaint.color = Color.WHITE
  dotPaint.style = Paint.Style.FILL
  canvas.drawCircle(size / 2f, size / 2f, dotRadius, dotPaint)

  return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Preview
@Composable
fun PreviewMapScreen() {
  AgriHealthAppTheme { MapScreen(startingPosition = Location(46.7990813, 6.6264253)) }
}
