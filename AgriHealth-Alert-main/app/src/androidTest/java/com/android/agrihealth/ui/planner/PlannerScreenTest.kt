package com.android.agrihealth.ui.planner

import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.android.agrihealth.data.model.firebase.emulators.FirebaseEmulatorsTest
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepositoryLocal
import com.android.agrihealth.testutil.TestConstants
import com.android.agrihealth.ui.navigation.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.IsoFields
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

object PlannerTestReportsData {
  val report1 =
      Report(
          "rep_id1",
          "Report title 1",
          "Description 1",
          emptyList(),
          null,
          "farmerId1",
          "vetId1",
          ReportStatus.PENDING,
          null,
          Location(46.9481, 7.4474, "Place name 1"),
          duration = LocalTime.of(0, 10))
  val report2 =
      report1.copy(id = "rep_id2", status = ReportStatus.RESOLVED, duration = LocalTime.of(1, 0))
  val report3 =
      report1.copy(
          id = "rep_id3", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10))
}

class PlannerScreenTest : FirebaseEmulatorsTest() {

  @get:Rule val composeTestRule = createComposeRule()

  val reportRepository = ReportRepositoryLocal()

  val today: LocalDate = LocalDate.now()
  val tuesday: LocalDate = today.with(DayOfWeek.TUESDAY)
  val sunday: LocalDate = today.with(DayOfWeek.SUNDAY)

  @Before fun setup() {}

  private fun setPlannerScreen(
      reportId: String? = null,
      goBack: () -> Unit = {},
      tabClicked: (Screen) -> Unit = {},
      reportClicked: (String) -> Unit = {},
  ) {
    composeTestRule.setContent {
      PlannerScreen(
          userId = PlannerTestReportsData.report1.officeId,
          reportId = reportId,
          goBack = goBack,
          tabClicked = tabClicked,
          reportClicked = reportClicked,
          plannerVM = PlannerViewModel(reportRepository),
      )
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun interactionTest() = runTest {
    var goBackCalled = false
    var tabClickedCalledWith: Screen? = null
    var reportClickedCalledWith: String? = null

    val report1 = PlannerTestReportsData.report1.copy(startTime = today.atTime(1, 0))

    runBlocking { reportRepository.addReport(report1) }

    setPlannerScreen(
        goBack = { goBackCalled = true },
        tabClicked = { screen -> tabClickedCalledWith = screen },
        reportClicked = { reportId -> reportClickedCalledWith = reportId })

    assertFalse(goBackCalled)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assertTrue(goBackCalled)
    assertNull(tabClickedCalledWith)
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.OVERVIEW_TAB)
        .assertIsDisplayed()
        .performClick()
    assertEquals(Screen.Overview, tabClickedCalledWith)
    assertNull(reportClickedCalledWith)
    scrollDailySchedulerToReportCardWithId(report1.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.reportCardTag(report1.id))
        .assertIsDisplayed()
        .performClick()
    assertEquals(report1.id, reportClickedCalledWith)
  }

  @Test
  fun testUnchangedDateAlert() = runTest {
    var goBackCalled = false
    val report1 = PlannerTestReportsData.report1.copy(startTime = null)

    runBlocking { reportRepository.addReport(report1) }

    setPlannerScreen(reportId = report1.id, goBack = { goBackCalled = true })
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX_CANCEL)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX).assertIsNotDisplayed()
    assertFalse(goBackCalled)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX_GO_BACK)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX).assertIsNotDisplayed()
    assertTrue(goBackCalled)
    goBackCalled = false
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.SET_REPORT_DATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.UNSAVED_ALERT_BOX).assertIsNotDisplayed()
    assertTrue(goBackCalled)
  }

  @Test
  fun displayedDatesAreCorrect() {
    setPlannerScreen()

    checkDisplayedDateInfoIsCorrect(today)

    composeTestRule.onNodeWithTag(PlannerScreenTestTags.dayCardTag(sunday)).performClick()
    checkDisplayedDateInfoIsCorrect(sunday)

    composeTestRule.onNodeWithTag(PlannerScreenTestTags.dayCardTag(tuesday)).performClick()
    checkDisplayedDateInfoIsCorrect(tuesday)
  }

  @Test
  fun reportsForSelectedDateAreCorrectlyDisplayed() = runTest {
    val otherDay = if (today == tuesday) sunday else tuesday
    // Add reports to repository
    val report1 = PlannerTestReportsData.report1.copy(startTime = today.atTime(6, 0))
    val report2 = PlannerTestReportsData.report2.copy(startTime = today.atTime(10, 0))
    val report3 = PlannerTestReportsData.report3.copy(startTime = otherDay.atTime(14, 0))

    runBlocking {
      reportRepository.addReport(report1)
      reportRepository.addReport(report2)
      reportRepository.addReport(report3)
    }
    Log.d(
        "PlannerTest",
        "Reports added to repository ${reportRepository.getAllReports(report1.officeId)}")

    setPlannerScreen()

    composeTestRule.waitUntil(TestConstants.DEFAULT_TIMEOUT) {
      scrollDailySchedulerToReportCardWithId(report2.id)
      composeTestRule.onNodeWithTag(PlannerScreenTestTags.reportCardTag(report2.id)).isDisplayed()
    }

    // Check reports for today
    scrollDailySchedulerToReportCardWithId(report1.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.reportCardTag(report1.id))
        .assertIsDisplayed()
    scrollDailySchedulerToReportCardWithId(report2.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.reportCardTag(report2.id))
        .assertIsDisplayed()

    assertReportNotInDailyScheduler(report3.id)

    // Change selected date to Other Day
    composeTestRule.onNodeWithTag(PlannerScreenTestTags.dayCardTag(otherDay)).performClick()
    composeTestRule.waitForIdle()

    // Check reports for Other Day
    assertReportNotInDailyScheduler(report1.id)

    assertReportNotInDailyScheduler(report2.id)

    scrollDailySchedulerToReportCardWithId(report3.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.reportCardTag(report3.id))
        .assertIsDisplayed()
  }

  @Test
  fun plannerScreenWithReportIdWorksCorrectly() = runTest {
    val report1 = PlannerTestReportsData.report1
    // Add reports to repository
    runBlocking { reportRepository.addReport(report1) }

    setPlannerScreen(reportId = report1.id)

    composeTestRule.onNodeWithTag(PlannerScreenTestTags.SET_REPORT_DATE_BOX).assertIsDisplayed()
    assertReportNotInDailyScheduler(report1.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.SET_REPORT_DATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    scrollDailySchedulerToReportCardWithId(report1.id)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.reportCardTag(report1.id))
        .assertIsDisplayed()
  }

  fun assertReportNotInDailyScheduler(reportId: String) {
    assertThrows(AssertionError::class.java) { scrollDailySchedulerToReportCardWithId(reportId) }
  }

  fun scrollDailySchedulerToReportCardWithId(reportId: String) {
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.DAILY_SCHEDULER)
        .performScrollToNode(hasTestTag(PlannerScreenTestTags.reportCardTag(reportId)))
  }

  fun checkDisplayedDateInfoIsCorrect(today: LocalDate) {
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.WEEK_NUMBER)
        .assertIsDisplayed()
        .assertTextIncludes("${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}", true)

    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.SELECTED_DATE)
        .assertIsDisplayed()
        .assertTextIncludes("${today.dayOfMonth}", true)
        .assertTextIncludes(today.month.name.take(3), ignoreCase = true)
    composeTestRule
        .onNodeWithTag(PlannerScreenTestTags.WEEK_HEADER)
        .assertIsDisplayed()
        .assertTextIncludes("${today.with(DayOfWeek.MONDAY).dayOfMonth}", ignoreCase = true)
        .assertTextIncludes("${today.with(DayOfWeek.SUNDAY).dayOfMonth}", ignoreCase = true)

    (0..6)
        .map { today.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
        .forEach { date ->
          composeTestRule
              .onNodeWithTag(PlannerScreenTestTags.dayCardTag(date))
              .assertIsDisplayed()
              .assertTextContains("${date.dayOfMonth}", ignoreCase = true)
        }
  }

  // Helper function to check if text includes a value can be used on Text() composable themselves
  fun SemanticsNodeInteraction.assertTextIncludes(
      value: String,
      ignoreCase: Boolean = false
  ): SemanticsNodeInteraction {
    val text = this.fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
    assertTrue("$text did not contain $value", text.contains(value, ignoreCase))
    return this
  }
}
