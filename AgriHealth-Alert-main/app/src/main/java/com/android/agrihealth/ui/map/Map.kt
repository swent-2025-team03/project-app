package com.android.agrihealth.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.device.location.LocationPermissionsRequester
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.ui.navigation.BottomNavigationMenu
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import com.android.agrihealth.ui.navigation.Tab
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest

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

  fun getTestTagForFilter(filter: String?): String = "filter_$filter"
}

const val AllFilterText = "All reports"

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    navigationActions: NavigationActions? = null,
    isViewedFromOverview: Boolean = true,
    forceStartingPosition: Boolean = false
) {
  // Get UI state
  val uiState by mapViewModel.uiState.collectAsState()

  val reports = uiState.reports
  val selectedReport by mapViewModel.selectedReport.collectAsState()

  fun Report.isSelected() = this == selectedReport
  fun Report.toggleSelect() = mapViewModel.setSelectedReport(if (this.isSelected()) null else this)

  // Marker filter
  var selectedFilter by remember { mutableStateOf<String?>(null) }

  // Initial camera state
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    mapViewModel.events.collectLatest { event: MapEvent ->
      when (event) {
        MapEvent.LoadingLocation -> snackbarHostState.showSnackbar("Loading location…")
      }
    }
  }

  val mapInitialLocation by mapViewModel.startingLocation.collectAsState()
  val mapInitialZoom by mapViewModel.zoom.collectAsState()
  val cameraPositionState = rememberCameraPositionState {}

  if (forceStartingPosition) mapViewModel.setStartingLocation(mapInitialLocation)

  LaunchedEffect(mapInitialLocation) {
    cameraPositionState.position =
        CameraPosition.fromLatLngZoom(
            LatLng(mapInitialLocation.latitude, mapInitialLocation.longitude), mapInitialZoom)
  }

  // Map settings and theme
  val (googleMapUiSettings, googleMapMapProperties) = getUISettingsAndTheme()

  // Floating button Position
  var reportInfoBoxHeightPx by remember { mutableIntStateOf(0) }

  Scaffold(
      topBar = { if (!isViewedFromOverview) MapTopBar(onBack = { navigationActions?.goBack() }) },
      bottomBar = {
        if (isViewedFromOverview)
            BottomNavigationMenu(
                selectedTab = Tab.Map,
                onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
                modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      content = { pd ->
        Box(modifier = Modifier.fillMaxSize().padding(pd)) {
          LocationPermissionsRequester(onComplete = { /* Set starting position? */})

          val reportsToDisplay =
              reports.filter {
                selectedFilter == null || it.report.status.displayString() == selectedFilter
              }

          GoogleMap(
              cameraPositionState = cameraPositionState,
              properties = googleMapMapProperties,
              uiSettings = googleMapUiSettings,
              modifier = Modifier.testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
              onMapClick = { mapViewModel.setSelectedReport(null) }) {
                ReportMarkers(
                    reports = reportsToDisplay,
                    isSelected = { it.isSelected() },
                    onClick = { it.toggleSelect() })

                AlertAreas()
              }

          MapTestMarkers(reportsToDisplay) { it.toggleSelect() }

          if (isViewedFromOverview) {
            MapFilterMenu(selectedFilter) { selectedFilter = it }
          }

          val density = LocalDensity.current
          val reportInfoBoxHeightDp = with(density) { reportInfoBoxHeightPx.toDp() }
          RefreshLocationButton(
              modifier =
                  Modifier.padding(
                          bottom = if (selectedReport != null) reportInfoBoxHeightDp else 0.dp)
                      .align(Alignment.BottomEnd)) {
                mapViewModel.refreshCameraPosition()

                val newPos = mapViewModel.startingLocation.value
                val newLat = newPos.latitude
                val newLng = newPos.longitude
                val newZoom = mapViewModel.zoom.value

                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(LatLng(newLat, newLng), newZoom))
              }

          ShowReportInfo(
              modifier =
                  Modifier.onGloballyPositioned { coordinates ->
                    reportInfoBoxHeightPx = coordinates.size.height
                  },
              report = selectedReport,
          ) {
            navigationActions?.navigateTo(Screen.ViewReport(it))
          }
        }
      })
}

// Preview composable functions
/*
// @Preview
@Composable
fun PreviewMapScreen() {
  LocationRepositoryProvider.repository = LocationRepository(LocalContext.current)
  val mapViewModel = MapViewModel(startingPosition = Location(46.7990813, 6.6264253), locationViewModel = LocationViewModel())
  AgriHealthAppTheme { MapScreen(mapViewModel) }
}

// @Preview
@Composable
fun PreviewDropdownMenu() {
  val options = listOf(null) + ReportStatus.entries.map { it.displayString() }
  var selectedFilter by remember { mutableStateOf<String?>(null) }
  AgriHealthAppTheme { FilterDropdown(options, selectedFilter) { selectedFilter = it } }
}

@Preview
@Composable
fun PreviewReportInfo() {
  val report =
      Report(
          id = "Test",
          title = "Veryyyyyyyyyyyyy long title",
          description =
              "very very very very very very very very very very very very very very very very very long description",
          questionForms = emptyList(),
          farmerId = "farmer id",
          officeId = "offId",
          status = ReportStatus.IN_PROGRESS,
          answer = "answer to the report",
          location = null,
          photoURL = null,
      )
  AgriHealthAppTheme {
    ShowReportInfo(
        report = report,
    )
  }
}
*/
