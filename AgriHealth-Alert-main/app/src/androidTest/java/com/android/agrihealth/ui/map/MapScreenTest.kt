package com.android.agrihealth.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.android.agrihealth.data.model.user.UserRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationRepository
import com.android.agrihealth.data.model.device.location.LocationRepositoryProvider
import com.android.agrihealth.data.model.device.location.LocationViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.alert.FakeAlertRepository
import com.android.agrihealth.testhelpers.fakes.FakeUserRepository
import com.android.agrihealth.testhelpers.fakes.InMemoryReportRepository
import com.android.agrihealth.testhelpers.TestConstants
import com.android.agrihealth.ui.navigation.NavigationActions
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

object MapScreenTestReports {
  val report1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          emptyList(),
          null,
          "farmerId1",
          "officeId1",
          ReportStatus.PENDING,
          null,
          Location(46.9481, 7.4474, "Place name 1"))
  val report2 =
      Report(
          "rep_id2",
          "Report title 2",
          "Description aaaa 2",
          emptyList(),
          null,
          "farmerId2",
          "officeId2",
          ReportStatus.IN_PROGRESS,
          "Vet answer",
          Location(46.9481, 7.4484))
  val report3 =
      Report(
          "rep_id3",
          "Report title 3",
          "Description 3",
          emptyList(),
          null,
          "farmerId3",
          "officeId1",
          ReportStatus.RESOLVED,
          null,
          Location(46.9481, 7.4464, "Place name 3"))
  val report4 =
      Report(
          "rep_id4",
          "Report title 4",
          "Description aaaa 4",
          emptyList(),
          null,
          "farmerId4",
          "officeId4",
          ReportStatus.SPAM,
          "Vet answer 4",
          Location(46.9491, 7.4474))
}

class MapScreenTest {

  private val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val ruleChain: TestRule =
      RuleChain.outerRule(
              GrantPermissionRule.grant(
                  android.Manifest.permission.ACCESS_FINE_LOCATION,
                  android.Manifest.permission.ACCESS_COARSE_LOCATION))
          .around(composeTestRule)

  private lateinit var locationViewModel: LocationViewModel
  private lateinit var locationRepository: LocationRepository
  val reportRepository = InMemoryReportRepository()
  val alertRepository = FakeAlertRepository()
  private lateinit var userId: String

  @Before
  fun setUp() = runTest {
    // Fake user
    userId = "TEST_USER"
    val fakeUser =
        Farmer(
            uid = userId,
            firstname = "Test",
            lastname = "User",
            email = "test@example.com",
            address = Location(46.9485, 7.4479),
            linkedOffices = emptyList(),
            defaultOffice = null,
            isGoogleAccount = false,
            description = null)

    val fakeUserRepository = FakeUserRepository(fakeUser)
    UserRepositoryProvider.repository = fakeUserRepository
    // Fake location repository
    locationRepository = mockk(relaxed = true)

    coEvery { locationRepository.getLastKnownLocation() } returns Location(46.9481, 7.4474, "Bern")

    coEvery { locationRepository.getCurrentLocation() } returns
        Location(46.9500, 7.4400, "Current Position")

    every { locationRepository.hasFineLocationPermission() } returns true
    every { locationRepository.hasCoarseLocationPermission() } returns true

    LocationRepositoryProvider.repository = locationRepository
    locationViewModel = LocationViewModel()

    // Inject test reports
    reportRepository.addReport(MapScreenTestReports.report1.copy(farmerId = userId))
    reportRepository.addReport(MapScreenTestReports.report2.copy(farmerId = userId))
    reportRepository.addReport(MapScreenTestReports.report3.copy(farmerId = userId))
    reportRepository.addReport(MapScreenTestReports.report4.copy(farmerId = userId))
  }

  // Sets composeTestRule to the map screen with a predefined MapViewModel, using the local report
  // repository among other things
  private fun setContentToMapWithVM(
      isViewedFromOverview: Boolean = true,
      selectedReportId: String? = null
  ) {
    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            locationViewModel = locationViewModel,
            selectedReportId = selectedReportId,
            userId = userId)
    composeTestRule.setContent {
      MaterialTheme {
        MapScreen(mapViewModel = mapViewModel, isViewedFromOverview = isViewedFromOverview)
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

  private fun assertMapScreenElementsVisibility(isViewedFromOverview: Boolean) {
    composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()

    if (isViewedFromOverview) {
      composeTestRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsNotDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsNotDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
      composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsDisplayed()
    } else {
      composeTestRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
          .assertIsNotDisplayed()
      composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsNotDisplayed()
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.VISIBILITY_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_VISIBILITY_SWITCH).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ALERT_VISIBILITY_SWITCH).assertIsDisplayed()
  }

  @Test
  fun displayReportsFromUser() = runTest {
    setContentToMapWithVM()

    composeTestRule.waitForIdle()

    val reports = reportRepository.getAllReports(userId)

    reports.forEach { report ->
      val markerTag = MapScreenTestTags.getTestTagForReportMarker(report.id)

      composeTestRule
          .onNodeWithTag(markerTag, useUnmergedTree = true)
          .assertExists()
          .assertIsDisplayed()
    }
  }

  @Test
  fun displayReportInfo() = runTest {
    setContentToMapWithVM()

    val report =
        reportRepository
            .getAllReports(userId)
            .last() // because of debug boxes, they stack so you have to take the last
    val reportId = report.id

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.ALERT_VISIBILITY_SWITCH)
        .performClick()
        .assertIsOff()

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(reportId))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportTitle(reportId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportDesc(reportId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(reportId))
        .performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsNotDisplayed()
  }

  @Test
  fun displayAlertInfo() = runTest {
    setContentToMapWithVM()

    val alert = alertRepository.getAlerts().last()
    val alertId = alert.id

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForAlertZone(alertId))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForAlertTitle(alertId))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.getTestTagForAlertDesc(alertId))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.getTestTagForAlertZone(alertId)).performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsNotDisplayed()
  }

  @Test
  fun filterReportsByStatus() = runTest {
    setContentToMapWithVM()

    composeTestRule.waitForIdle()

    val reports = reportRepository.getAllReports(userId)

    composeTestRule.waitUntil(timeoutMillis = TestConstants.DEFAULT_TIMEOUT) {
      val count =
          reports.sumOf { r ->
            val tag = MapScreenTestTags.getTestTagForReportMarker(r.id)
            composeTestRule
                .onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .size
          }
      count == reports.size
    }

    // Show/hide works
    val reportSwitchNode = composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_VISIBILITY_SWITCH)
    reportSwitchNode.assertIsOn()
    reportSwitchNode.performClick()
    reportSwitchNode.assertIsOff()
    composeTestRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsNotDisplayed()
    reports.forEach {
      composeTestRule
          .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(it.id))
          .assertDoesNotExist()
    }
    reportSwitchNode.performClick()
    reportSwitchNode.assertIsOn()

    // If show, filters work
    val filters: List<String?> =
        listOf<String?>(null) + ReportStatus.entries.map { it.displayString() }

    filters.forEach { filter ->
      composeTestRule
          .onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU)
          .assertIsDisplayed()
          .performClick()
      composeTestRule
          .onNodeWithTag(MapScreenTestTags.getTestTagForFilter(filter), useUnmergedTree = true)
          .assertIsDisplayed()
          .performClick()
      val (matches, nonMatches) =
          reports.partition { it -> filter == null || it.status.displayString() == filter }
      matches.forEach { report ->
        composeTestRule
            .onNodeWithTag(
                MapScreenTestTags.getTestTagForReportMarker(report.id), useUnmergedTree = true)
            .assertExists()
            .assertIsDisplayed()
      }
      nonMatches.forEach { report ->
        composeTestRule
            .onNodeWithTag(
                MapScreenTestTags.getTestTagForReportMarker(report.id), useUnmergedTree = true)
            .assertDoesNotExist()
      }
    }
  }

  @Test
  fun showHideAlerts() = runTest {
    setContentToMapWithVM()

    val alerts = alertRepository.getAlerts()
    val alertSwitchNode = composeTestRule.onNodeWithTag(MapScreenTestTags.ALERT_VISIBILITY_SWITCH)
    val alertsNodes =
        alerts.map { alert ->
          composeTestRule.onNodeWithTag(MapScreenTestTags.getTestTagForAlertZone(alert.id))
        }

    alertSwitchNode.assertIsOn()
    alertsNodes.forEach { node -> node.assertIsDisplayed() }
    alertSwitchNode.performClick()
    alertSwitchNode.assertIsOff()
    alertsNodes.forEach { node -> node.assertIsNotDisplayed() }
    alertSwitchNode.performClick()
    alertSwitchNode.assertIsOn()
  }

  @Test
  fun canNavigateFromMapToReport() = runTest {
    val reports = reportRepository.getAllReports(userId)
    val report = reports.first()
    val reportId = report.id
    val FAKE_VIEW_REPORT = "fakeViewReportScreen"

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = NavigationActions(navController)

      NavHost(navController = navController, startDestination = Screen.Map.route) {
        composable(Screen.Map.route) {
          val mapViewModel =
              MapViewModel(
                  reportRepository = reportRepository,
                  locationViewModel = locationViewModel,
                  selectedReportId = reportId,
                  userId = userId)
          MapScreen(mapViewModel = mapViewModel, navigationActions = navigation)
        }
        composable(Screen.ViewReport.route) {
          Text(reportId, modifier = Modifier.testTag(FAKE_VIEW_REPORT))
        }
      }
    }

    // Go to map screen
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    // Click on report
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.INFO_NAVIGATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Check if report screen
    composeTestRule.onNodeWithTag(FAKE_VIEW_REPORT).assertIsDisplayed().assertTextContains(reportId)
  }

  @Test
  fun canNavigateFromMapToAlert() = runTest {
    val alert = alertRepository.getAlerts().last()
    val alertId = alert.id
    val FAKE_ALERT_VIEW = "fakeAlertViewScreen"

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = NavigationActions(navController)

      NavHost(navController = navController, startDestination = Screen.Map.route) {
        composable(Screen.Map.route) {
          val mapViewModel =
              MapViewModel(
                  reportRepository = reportRepository,
                  locationViewModel = locationViewModel,
                  userId = userId)
          MapScreen(mapViewModel = mapViewModel, navigationActions = navigation)
        }
        composable(Screen.ViewAlert.route) {
          Text(alertId, modifier = Modifier.testTag(FAKE_ALERT_VIEW))
        }
      }
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.getTestTagForAlertZone(alert.id)).performClick()
    composeTestRule.onNodeWithTag(MapScreenTestTags.INFO_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MapScreenTestTags.INFO_NAVIGATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(FAKE_ALERT_VIEW).assertIsDisplayed().assertTextContains(alertId)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun spiderifyReportsTest() = runTest {
    reportRepository.addReport(MapScreenTestReports.report1.copy(farmerId = userId))

    val mapViewModel =
        MapViewModel(
            reportRepository = reportRepository,
            locationViewModel = locationViewModel,
            userId = userId,
            selectedReportId = MapScreenTestReports.report1.id)

    val reports1 = List(10) { index -> MapScreenTestReports.report1.copy(id = "report1$index") }
    val reports2 = List(5) { index -> MapScreenTestReports.report2.copy(id = "report2$index") }
    val reports = reports1 + reports2

    reports.forEach { it -> reportRepository.addReport(it.copy(farmerId = userId)) }
    advanceUntilIdle()
    mapViewModel.refreshReports()

    val uiState = mapViewModel.uiState

    uiState.map { it.reports }.first { it.isNotEmpty() }
    advanceUntilIdle()

    val spiderifiedReport = uiState.value.reports
    val groups = spiderifiedReport.groupBy { it.center }

    val group1 =
        groups[
                LatLng(
                    MapScreenTestReports.report1.location?.latitude ?: 0.0,
                    MapScreenTestReports.report1.location?.longitude ?: 0.0)]
            ?.toSet()
    val group2 =
        groups[
                LatLng(
                    MapScreenTestReports.report2.location?.latitude ?: 0.0,
                    MapScreenTestReports.report2.location?.longitude ?: 0.0)]
            ?.toSet()

    assertEquals(11, group1?.size)
    assertEquals(6, group2?.size)

    val positions1 = group1?.map { it.position }?.toSet()
    val positions2 = group2?.map { it.position }?.toSet()

    assertEquals(11, positions1?.size)
    assertEquals(6, positions2?.size)
  }
}
