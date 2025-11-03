package com.android.agrihealth.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.repository.ReportRepositoryLocal
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.test.runTest
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

  val reportRepository = ReportRepositoryLocal()
  val userId = UserViewModel().user.value.uid

  @Before
  override fun setUp() {
    super.setUp()
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
        MapViewModel(reportRepository = reportRepository, selectedReportId = selectedReportId)
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
          .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(report.id), useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun displayReportInfo() = runTest {
    setContentToMapWithVM()

    val report = reportRepository.getReportsByFarmer(userId).last() // because of debug boxes, they stack so you have to take the last
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
      composeRule
          .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(reportId))
          .performClick()
      composeRule.onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX).assertIsNotDisplayed()
  }

  @Test
  fun filterReportsByStatus() = runTest {
    setContentToMapWithVM()
    val reports = reportRepository.getReportsByFarmer(userId)
    val filters = listOf("All") + ReportStatus.entries.map { it.displayString() }

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
          reports.partition { it -> filter == "All" || it.status.displayString() == filter }
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
}
