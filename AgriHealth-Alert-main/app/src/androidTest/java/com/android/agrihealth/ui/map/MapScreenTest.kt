package com.android.agrihealth.ui.map

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.testhelpers.TestReport
import com.android.agrihealth.testhelpers.TestUser.farmer1
import com.android.agrihealth.testhelpers.fakes.FakeAlertRepository
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MapScreenTest :
    UITest(
        grantedPermissions =
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
  val userId = farmer1.uid
  val reportRepository =
      FakeReportRepository(
          listOf(
              TestReport.report1.copy(farmerId = userId),
              TestReport.report2.copy(farmerId = userId),
              TestReport.report3.copy(farmerId = userId),
              TestReport.report4.copy(farmerId = userId)))
  val alertRepository = FakeAlertRepository()
  val userRepository = FakeUserRepository(farmer1)
  val locationViewModel = LocationViewModel(fakeLocationRepository())

  private fun fakeLocationRepository(delayMs: Long = 0L): LocationRepository {
    val repo: LocationRepository = mockk(relaxed = true)

    coEvery { repo.getLastKnownLocation() } coAnswers
        {
          delay(delayMs)
          Location(46.9481, 7.4474, "Bern")
        }
    coEvery { repo.getCurrentLocation() } coAnswers
        {
          delay(delayMs)
          Location(46.9500, 7.4400, "Current Position")
        }

    return repo
  }

  // Sets composeTestRule to the map screen with a predefined MapViewModel, using the local report
  // repository among other things
  private fun setContentToMapWithVM(
      isViewedFromOverview: Boolean = true,
      selectedReportId: String? = null,
      locationVM: LocationViewModel = locationViewModel
  ): MapViewModel {
    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            userRepository = userRepository,
            locationViewModel = locationVM,
            alertRepository = alertRepository,
            selectedReportId = selectedReportId,
            userId = userId)

    setContent { MapScreen(mapViewModel, isViewedFromOverview = isViewedFromOverview) }

    return mapViewModel
  }

  override fun displayAllComponents() {}

  private fun assertMapScreenElementsVisibility(isViewedFromOverview: Boolean) {
    with(MapScreenTestTags) {
      nodesAreDisplayed(
          GOOGLE_MAP_SCREEN,
          REFRESH_BUTTON,
          VISIBILITY_MENU,
          REPORT_VISIBILITY_SWITCH,
          ALERT_VISIBILITY_SWITCH)

      if (isViewedFromOverview) {
        nodesAreDisplayed(REPORT_FILTER_MENU, NavigationTestTags.BOTTOM_NAVIGATION_MENU)
        nodesNotDisplayed(TOP_BAR_MAP_TITLE, NavigationTestTags.GO_BACK_BUTTON)
      } else {
        nodesAreDisplayed(TOP_BAR_MAP_TITLE, NavigationTestTags.GO_BACK_BUTTON)
        nodesNotDisplayed(REPORT_FILTER_MENU, NavigationTestTags.BOTTOM_NAVIGATION_MENU)
      }
    }
  }

  @Test
  fun displayAllFieldsAndButtonsFromOverview() {
    setContentToMapWithVM(isViewedFromOverview = true)
    assertMapScreenElementsVisibility(isViewedFromOverview = true)
  }

  @Test
  fun displayAllFieldsAndButtonsFromReportView() {
    setContentToMapWithVM(isViewedFromOverview = false)
    assertMapScreenElementsVisibility(isViewedFromOverview = false)
  }

  @Test
  fun displayReportsFromUser_withFiltersAndSwitch_andShowsInfo() = runTest {
    fun List<Report>.assertAll(assertion: (tag: String) -> Unit) {
      this.forEach { report ->
        val markerTag = MapScreenTestTags.getTestTagForReportMarker(report.id)
        assertion(markerTag)
      }
    }

    fun List<Report>.assertAllDisplayed() = this.assertAll(::nodeIsDisplayed)
    fun List<Report>.assertNoneDisplayed() = this.assertAll(::nodeNotDisplayed)

    setContentToMapWithVM()

    val reports = reportRepository.getAllReports(userId)

    with(MapScreenTestTags) {
      // Disable alerts to prevent issues
      clickOn(ALERT_VISIBILITY_SWITCH)

      // No filter, switch is on
      reports.assertAllDisplayed()

      // Show/hide works
      val reportSwitch = node(REPORT_VISIBILITY_SWITCH)
      reportSwitch.assertIsOn()
      reportSwitch.performClick()
      reportSwitch.assertIsOff()

      nodeNotDisplayed(REPORT_FILTER_MENU)
      reports.assertNoneDisplayed()

      reportSwitch.performClick()
      reportSwitch.assertIsOn()

      // If show, filters work
      val filters: List<String?> =
          listOf<String?>(null) + ReportStatus.entries.map { it.displayString() }

      filters.forEach { filter ->
        clickOn(REPORT_FILTER_MENU)
        clickOn(getTestTagForFilter(filter))

        val (matches, nonMatches) =
            reports.partition { it -> filter == null || it.status.displayString() == filter }

        matches.assertAllDisplayed()
        nonMatches.assertNoneDisplayed()

        // Info box
        val reportId = matches.last().id
        clickOn(getTestTagForReportMarker(reportId))
        nodesAreDisplayed(
            INFO_BOX, getTestTagForReportTitle(reportId), getTestTagForReportDesc(reportId))
        clickOn(getTestTagForReportMarker(reportId))
        nodeNotDisplayed(INFO_BOX)
      }
    }
  }

  @Test
  fun displayAlert_withSwitch_andShowsInfo() = runTest {
    fun List<Alert>.assertAll(assertion: (tag: String) -> Unit) {
      this.forEach { alert ->
        val zoneTag = MapScreenTestTags.getTestTagForAlertZone(alert.id)
        assertion(zoneTag)
      }
    }

    fun List<Alert>.assertAllDisplayed() = this.assertAll(::nodeIsDisplayed)
    fun List<Alert>.assertNoneDisplayed() = this.assertAll(::nodesNotDisplayed)

    setContentToMapWithVM()

    val alerts = alertRepository.getAlerts()

    with(MapScreenTestTags) {
      // Show/hide works
      alerts.assertAllDisplayed()

      val alertSwitch = node(ALERT_VISIBILITY_SWITCH)
      alertSwitch.assertIsOn()
      alertSwitch.performClick()
      alertSwitch.assertIsOff()

      alerts.assertNoneDisplayed()

      alertSwitch.performClick()
      alertSwitch.assertIsOn()

      // Info box
      val alertId = alerts.last().id
      clickOn(getTestTagForAlertZone(alertId))
      nodesAreDisplayed(INFO_BOX, getTestTagForAlertTitle(alertId), getTestTagForAlertDesc(alertId))
      clickOn(getTestTagForAlertZone(alertId))
      nodeNotDisplayed(INFO_BOX)
    }
  }

  @Test
  fun navigationFromMap_toReportAndToAlert() = runTest {
    val report = reportRepository.getAllReports(userId).last()
    val reportId = report.id
    val FAKE_VIEW_REPORT = "fakeViewReportScreen"

    val alert = alertRepository.getAlerts().last()
    val alertId = alert.id
    val FAKE_ALERT_VIEW = "fakeAlertViewScreen"

    val GO_BACK = "fakeGoBack"

    lateinit var navigationActions: NavigationActions

    setContent {
      val navController = rememberNavController()
      navigationActions = NavigationActions(navController)

      NavHost(navController = navController, startDestination = Screen.Map.route) {
        composable(Screen.Map.route) {
          val mapViewModel =
              MapViewModel(
                  reportRepository = reportRepository,
                  alertRepository = alertRepository,
                  locationViewModel = locationViewModel,
                  userId = userId)
          MapScreen(mapViewModel = mapViewModel, navigationActions = navigationActions)
        }
        composable(Screen.ViewReport.route) {
          Text(reportId, modifier = Modifier.testTag(FAKE_VIEW_REPORT))
          Button(onClick = { navigationActions.goBack() }, modifier = Modifier.testTag(GO_BACK)) {
            Text(":)")
          }
        }
        composable(Screen.ViewAlert.route) {
          Text(alertId, modifier = Modifier.testTag(FAKE_ALERT_VIEW))
        }
      }
    }

    with(MapScreenTestTags) {
      clickOn(NavigationTestTags.MAP_TAB)
      nodeIsDisplayed(GOOGLE_MAP_SCREEN)

      // Report
      clickOn(ALERT_VISIBILITY_SWITCH)
      clickOn(getTestTagForReportMarker(reportId))
      nodeIsDisplayed(INFO_BOX)
      clickOn(INFO_NAVIGATION_BUTTON)
      textContains(FAKE_VIEW_REPORT, reportId)

      clickOn(GO_BACK)
      clickOn(REPORT_VISIBILITY_SWITCH)

      // Alert
      clickOn(getTestTagForAlertZone(alertId))
      nodeIsDisplayed(INFO_BOX)
      clickOn(INFO_NAVIGATION_BUTTON)
      textContains(FAKE_ALERT_VIEW, alertId)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun spiderifyReportsTest() = runTest {
    reportRepository.addReport(TestReport.report1.copy(farmerId = userId))

    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            alertRepository = alertRepository,
            locationViewModel = locationViewModel,
            userId = userId,
            selectedReportId = TestReport.report1.id)

    val reports1 = List(10) { index -> TestReport.report1.copy(id = "report1$index") }
    val reports2 = List(5) { index -> TestReport.report2.copy(id = "report2$index") }
    val reports = reports1 + reports2

    reports.forEach { it -> reportRepository.addReport(it.copy(farmerId = userId)) }
    advanceUntilIdle()
    mapViewModel.refreshReports()

    val spiderifiedReport = mapViewModel.uiState.map { it.reports }.first { it.size == 19 }

    val groups = spiderifiedReport.groupBy { it.center }

    val group1 =
        groups[
                LatLng(
                    TestReport.report1.location?.latitude ?: 0.0,
                    TestReport.report1.location?.longitude ?: 0.0)]
            ?.toSet()
    val group2 =
        groups[
                LatLng(
                    TestReport.report2.location?.latitude ?: 0.0,
                    TestReport.report2.location?.longitude ?: 0.0)]
            ?.toSet()

    assertEquals(11, group1?.size)
    assertEquals(6, group2?.size)

    val positions1 = group1?.map { it.position }?.toSet()
    val positions2 = group2?.map { it.position }?.toSet()

    assertEquals(11, positions1?.size)
    assertEquals(6, positions2?.size)
  }
}
