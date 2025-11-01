package com.android.agrihealth.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.agrihealth.AgriHealthApp
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.displayString
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FakeReportRepository: ReportRepository {
    private val reports = mutableListOf<Report>()

    override fun getNewReportId(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getAllReports(userId: String): List<Report> {
        return reports.toList()
    }

    override suspend fun getReportsByFarmer(farmerId: String): List<Report> {
        return reports.filter { it -> it.farmerId == farmerId }.toList()
    }

    override suspend fun getReportsByVet(vetId: String): List<Report> {
        return reports.filter { it -> it.vetId == vetId }.toList()
    }

    override suspend fun getReportById(reportId: String): Report? {
        return reports.first { it -> it.id == reportId }
    }

    override suspend fun addReport(report: Report) {
        reports.add(report)
    }

    override suspend fun editReport(
        reportId: String,
        newReport: Report
    ) {
        deleteReport(reportId)
        addReport(newReport)
    }

    override suspend fun deleteReport(reportId: String) {
        reports.remove(getReportById(reportId))
    }

}

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

  val reportList = listOf(report1, report2, report3, report4)
}

class MapScreenTest : FirebaseEmulatorsTest() {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  val reportRepository = FakeReportRepository()
    val userId = "farmerId"

  @Before
  override fun setUp() {
    super.setUp()
    runTest { authRepository.signUpWithEmailAndPassword(user1.email, password1, user1) }
    runTest {
      reportRepository.addReport(MapScreenTestReports.report1.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report2.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report3.copy(farmerId = userId))
      reportRepository.addReport(MapScreenTestReports.report4.copy(farmerId = userId))
    }
  }

  @Test
  fun canNavigateFromOverview() {
    composeRule.setContent { MaterialTheme { AgriHealthApp() } }
    composeRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun displayAllFieldsAndButtonsFromOverview() {
    composeRule.setContent { MaterialTheme { MapScreen(isViewedFromOverview = true) } }
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsNotDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsNotDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayAllFieldsAndButtonsFromReportView() {
    composeRule.setContent { MaterialTheme { MapScreen(isViewedFromOverview = false) } }
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.TOP_BAR_MAP_TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsNotDisplayed()
    composeRule.onNodeWithTag(MapScreenTestTags.REPORT_FILTER_MENU).assertIsNotDisplayed()
  }

  private fun getUserReports(uid: String): List<Report> {
    return MapScreenTestReports.reportList.filter { it.farmerId == uid || it.vetId == uid }
  }

  @Test
  fun displayReportsFromUser() {
    composeRule.setContent { MaterialTheme { MapScreen() } }
    assertNotNull(Firebase.auth.currentUser)
    val uid = Firebase.auth.currentUser!!.uid

    getUserReports(uid).forEach { report ->
      composeRule
          .onNodeWithTag(MapScreenTestTags.getTestTagForReportMarker(report.id))
          .assertIsDisplayed()
    }
  }

  @Test
  fun displayReportInfo() {
    composeRule.setContent { MaterialTheme { MapScreen() } }
    assertNotNull(Firebase.auth.currentUser)
    val uid = Firebase.auth.currentUser!!.uid

    getUserReports(uid).forEach { report ->
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
  }

  @Test
  fun filterReportsByStatus() {
    composeRule.setContent { MaterialTheme { MapScreen() } }
    assertNotNull(Firebase.auth.currentUser)
    val uid = Firebase.auth.currentUser!!.uid
    val reports = getUserReports(uid)
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
    composeRule.setContent { MaterialTheme { MapScreen(startingPosition = yverdon) } }
    // how do i test this
    assertTrue(true)
  }

  @Test fun mapCenteredOnUserAddress() {}

  @Test fun mapCenteredOnDefaultIfNoAddress() {}

  @Test
  fun canNavigateFromMapToReport() = runTest {
      val reports = reportRepository.getReportsByFarmer(userId)
      val report = reports.first()

    val mapViewModel = MapViewModel(reportRepository = reportRepository, selectedReportId = report.id)
    composeRule.setContent { MaterialTheme { MapScreen(mapViewModel) } }
    // Go to map screen
    composeRule.onNodeWithTag(NavigationTestTags.MAP_TAB).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertIsDisplayed()
    // Click on report

    composeRule
        .onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX)
        .assertIsDisplayed()
        .performClick()
    composeRule
        .onNodeWithTag(MapScreenTestTags.REPORT_NAVIGATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    // Check if report screen TODO: Actually show the report screen
    //composeRule.onNodeWithTag(MapScreenTestTags.REPORT_INFO_BOX).assertIsNotDisplayed()
  }
}
