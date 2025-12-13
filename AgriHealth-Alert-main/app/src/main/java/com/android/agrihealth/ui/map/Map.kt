package com.android.agrihealth.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.android.agrihealth.data.model.alert.Alert
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
  val alerts = uiState.alerts
  var selectedAlerts by remember { mutableStateOf(listOf<Alert>()) }

  fun Report.isSelected() = this == selectedReport
  fun Report.toggleSelect() {
    mapViewModel.setSelectedReport(if (this.isSelected()) null else this)
    selectedAlerts = listOf()
  }

  // Marker filter
  var selectedFilter by remember { mutableStateOf<String?>(null) }

  // Alert filter
  var showAlerts by remember { mutableStateOf(true) }

  // Initial camera state
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.isLoadingLocation) {
    if (uiState.isLoadingLocation) {
      snackbarHostState.showSnackbar("Loading location...")
    }
  }

  val mapInitialLocation by mapViewModel.startingLocation.collectAsState()
  val mapInitialZoom by mapViewModel.zoom.collectAsState()
  val cameraPositionState = rememberCameraPositionState {}

  var forcedOnce by remember { mutableStateOf(false) }

  LaunchedEffect(forceStartingPosition) {
    if (forceStartingPosition && !forcedOnce) {
      forcedOnce = true
      mapViewModel.setStartingLocation(mapViewModel.startingLocation.value)
    }
  }

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

          val alertsToDisplay = alerts.filter { showAlerts }

          GoogleMap(
              cameraPositionState = cameraPositionState,
              properties = googleMapMapProperties,
              uiSettings = googleMapUiSettings,
              modifier = Modifier.testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
              onMapClick = { tapLatLng ->
                // Reports: unselect
                mapViewModel.setSelectedReport(null)

                // Alerts: show info for every potential
                val newAlerts = findAlertZonesUnderTap(alertsToDisplay, tapLatLng)
                selectedAlerts = if (newAlerts == selectedAlerts) listOf() else newAlerts
              }) {
                ReportMarkers(
                    reports = reportsToDisplay,
                    isSelected = { it.isSelected() },
                    onClick = { it.toggleSelect() })

                AlertAreas(alerts = alertsToDisplay)
              }

          MapTestReportMarkers(reportsToDisplay) { it.toggleSelect() }
          MapTestAlertCircles(alertsToDisplay) {
            val newList = listOf(it)
            selectedAlerts = if (newList == selectedAlerts) listOf() else newList
          }

          // Control what to display
          Column(
              modifier =
                  Modifier.padding(16.dp)
                      .align(Alignment.TopEnd)
                      .background(
                          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6F),
                          shape = RoundedCornerShape(16.dp))
                      .testTag(MapScreenTestTags.VISIBILITY_MENU),
              horizontalAlignment = Alignment.End) {
                val showReports = selectedFilter != "None"

                AlertVisibilitySwitch(showAlerts) {
                  showAlerts = it
                  selectedAlerts = listOf()
                }
                ReportVisibilitySwitch(showReports) { enabled ->
                  selectedFilter = if (enabled) null else "None"
                }

                if (isViewedFromOverview && showReports) {
                  MapFilterMenu(selectedFilter) { selectedFilter = it }
                }
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

          ShowAlertInfo(selectedAlerts) { alert ->
            navigationActions?.navigateTo(Screen.ViewAlert(alert.id))
          }
        }
      })
}

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "googleMapScreen"
  const val TOP_BAR_MAP_TITLE = "topBarMapTitle"
  const val INFO_BOX = "infoBox"
  const val REPORT_FILTER_MENU = "reportFilterDropdownMenu"
  const val INFO_NAVIGATION_BUTTON = "navigationButton"
  const val REFRESH_BUTTON = "mapRefreshButton"
  const val VISIBILITY_MENU = "mapDisplayVisibilityMenu"
  const val REPORT_VISIBILITY_SWITCH = "reportVisibilitySwitch"
  const val ALERT_VISIBILITY_SWITCH = "alertVisibilitySwitch"

  // from bootcamp map
  fun getTestTagForReportMarker(reportId: String): String = "reportMarker_$reportId"

  fun getTestTagForReportTitle(reportId: String): String = "reportTitle_$reportId"

  fun getTestTagForReportDesc(reportId: String): String = "reportDescription_$reportId"

  fun getTestTagForAlertZone(alertId: String): String = "alertZone_$alertId"

  fun getTestTagForAlertTitle(alertId: String): String = "alertTitle_$alertId"

  fun getTestTagForAlertDesc(alertId: String): String = "alertDescription_$alertId"

  fun getTestTagForFilter(filter: String?): String = "filter_$filter"
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
