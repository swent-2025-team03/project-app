package com.android.agrihealth.ui.map

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.repository.ReportRepositoryLocal
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.user.UserViewModel
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

object MapScreenTestReports {
  val report1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          null,
          "farmerId1",
          "vetId1",
          ReportStatus.PENDING,
          null,
          Location(46.9481, 7.4474, "Place name 1"))
  val report2 =
      Report(
          "rep_id2",
          "Report title 2",
          "Description aaaa 2",
          null,
          "farmerId2",
          "vetId2",
          ReportStatus.IN_PROGRESS,
          "Vet answer",
          Location(46.9481, 7.4484))
  val report3 =
      Report(
          "rep_id3",
          "Report title 3",
          "Description 3",
          null,
          "farmerId3",
          "vetId1",
          ReportStatus.RESOLVED,
          null,
          Location(46.9481, 7.4464, "Place name 3"))
  val report4 =
      Report(
          "rep_id4",
          "Report title 4",
          "Description aaaa 4",
          null,
          "farmerId4",
          "vetId4",
          ReportStatus.SPAM,
          "Vet answer 4",
          Location(46.9491, 7.4474))
}

class MapScreenTest : FirebaseEmulatorsTest() {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION)

  private lateinit var locationViewModel: LocationViewModel
  private lateinit var locationRepository: LocationRepository
  val reportRepository = ReportRepositoryLocal()
  val userId = UserViewModel().user.value.uid

  @Before
  override fun setUp() {
    super.setUp()
    locationRepository = mockk(relaxed = true)

    coEvery { locationRepository.getLastKnownLocation() } returns Location(46.9481, 7.4474, "Bern")

    coEvery { locationRepository.getCurrentLocation() } returns
        Location(46.9500, 7.4400, "Current Position")

    every { locationRepository.hasFineLocationPermission() } returns true
    every { locationRepository.hasCoarseLocationPermission() } returns true

    LocationRepositoryProvider.repository = locationRepository
    locationViewModel = LocationViewModel()
    runTest {
      reportRepository.addReport(MapScreenTestReports.report1.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report2.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report3.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report4.copy(farmerId = userId))
    }
  }

  // Sets composeRule to the map screen with a predefined MapViewModel, using the local report
  // repository among other things
  private fun setContentToMapWithVM(
      isViewedFromOverview: Boolean = true,
      selectedReportId: String? = null,
      startingPosition: Location? = null
  ) {
    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            locationViewModel = locationViewModel,
            selectedReportId = selectedReportId)
    composeRule.setContent {
      MaterialTheme {
        MapScreen(
            mapViewModel = mapViewModel,
            isViewedFromOverview = isViewedFromOverview,
            startingPosition = startingPosition)
      }
    }
  }

  @Test
  fun displayAllFieldsAndButtonsFromOverview() {
    setContentToMapWithVM(isViewedFromOverview = true)
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsNotDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsNotDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayAllFieldsAndButtonsFromReportView() {
    setContentToMapWithVM(isViewedFromOverview = false)
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsNotDisplayed()
  }

  @Test
  fun displayReportsFromUser() = runTest {
    setContentToMapWithVM()

    reportRepository.getReportsByFarmer(userId).forEach { report ->
      composeRule
          .onNodeWithTag(
              MapScreenTestTags.getTestTagForReportMarker(report.id), useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun displayReportInfo() = runTest {
    setContentToMapWithVM()

    val report =
        reportRepository
            .getReportsByFarmer(userId)
            .last() // because of debug boxes, they stack so you have to take the last
    val reportId = report.id
    composeRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(reportId))
        .assertIsDisplayed()
        .performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX).assertIsDisplayed()
    composeRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportTitle(reportId))
        .assertIsDisplayed()
    composeRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportDesc(reportId))
        .assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(reportId)).performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX).assertIsNotDisplayed()
  }

  @Test
  fun filterReportsByStatus() = runTest {
    setContentToMapWithVM()
    val reports = reportRepository.getReportsByFarmer(userId)
    val filters = listOf("All reports") + ReportStatus.entries.map { it.displayString() }

    filters.forEach { filter ->
      composeRule
          .onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU)
          .assertIsDisplayed()
          .performClick()
      composeRule
          .onNodeWithTag(MapScreenTestTags.getTestTagForFilter(filter))
          .assertIsDisplayed()
          .performClick()
      val (matches, nonMatches) =
          reports.partition { it -> filter == "All reports" || it.status.displayString() == filter }
      matches.forEach { report ->
        composeRule
            .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(report.id))
            .assertIsDisplayed()
      }
      nonMatches.forEach { report ->
        composeRule
            .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(report.id))
            .assertIsNotDisplayed()
      }
    }
  }

  @Test
  fun canOverrideStartingPosition() {
    val yverdon = Location(46.7815062, 6.6463836) // Station d'épuration d'Yverdon-les-Bains
    setContentToMapWithVM(startingPosition = yverdon)
    // how do i test this
    assertTrue(true)
  }

  @Test fun mapCenteredOnUserAddress() {}

  @Test fun mapCenteredOnDefaultIfNoAddress() {}

  @Test
  fun canNavigateFromMapToReport() = runTest {
    val reports = reportRepository.getReportsByFarmer(userId)
    val report = reports.first()
    setContentToMapWithVM(selectedReportId = report.id)

    // Go to map screen
    composeRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    // Click on report
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX).assertIsDisplayed().performClick()
    composeRule
        .onNodeWithTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    // Check if report screen TODO: Actually show the report screen
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun spiderifyReportsTest() = runTest {
    reportRepository.addReport(MapScreenTestReports.report1.copy(farmerId = userId))

    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            locationViewModel = locationViewModel,
            selectedReportId = MapScreenTestReports.report1.id)

    val reports1 = List(10) { index -> MapScreenTestReports.report1.copy(id = "report1$index") }
    val reports2 = List(5) { index -> MapScreenTestReports.report2.copy(id = "report2$index") }
    val reports = reports1 + reports2
    reports.forEach { it -> reportRepository.addReport(it.copy(farmerId = userId)) }
    advanceUntilIdle()
    mapViewModel.refreshReports(userId)
    mapViewModel.uiState.map { it.reports }.first { it.isNotEmpty() }
    advanceUntilIdle()
    Log.d("MapTests", "Reports: ${mapViewModel.uiState.value.reports}")
    val spiderifiedReport = mapViewModel.spiderifiedReports()
    Log.d("MapTests", "SpidReports: $spiderifiedReport")
    val groups = spiderifiedReport.groupBy { it.center }
    Log.d("MapTests", "Groups: $groups")

    val group1 =
        groups[
                LatLng(
                    MapScreenTestReports.report1.location?.latitude ?: 0.0,
                    MapScreenTestReports.report1.location?.longitude ?: 0.0)]
            ?.toSet()
    Log.d("MapTests", "Group1: $group1")
    val group2 =
        groups[
                LatLng(
                    MapScreenTestReports.report2.location?.latitude ?: 0.0,
                    MapScreenTestReports.report2.location?.longitude ?: 0.0)]
            ?.toSet()
    Log.d("MapTests", "Group2: $group2")

    assertEquals(11, group1?.size)
    assertEquals(6, group2?.size)

    val positions1 = group1?.map { it.position }?.toSet()
    val positions2 = group2?.map { it.position }?.toSet()

    assertEquals(11, positions1?.size)
    assertEquals(6, positions2?.size)
    Log.d("MapTests", "pos1: $positions1")
    Log.d("MapTests", "pos2: $positions2")
  }
}
