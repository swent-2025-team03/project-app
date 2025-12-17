package com.android.agrihealth.ui.planner

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.testhelpers.LoadingOverlayTestUtils.assertOverlayDuringLoading
import com.android.agrihealth.testhelpers.TestReport
import com.android.agrihealth.testhelpers.TestTimeout.SHORT_TIMEOUT
import com.android.agrihealth.testhelpers.TestUser
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.testhelpers.templates.UITest
import com.android.agrihealth.ui.common.layout.NavigationTestTags
import com.android.agrihealth.ui.navigation.Screen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.IsoFields
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerScreenTest : UITest() {
  val reportRepository = FakeReportRepository()

  val today: LocalDate = LocalDate.now()
  val tuesday: LocalDate = today.with(DayOfWeek.TUESDAY)
  val sunday: LocalDate = today.with(DayOfWeek.SUNDAY)

  val farmer = TestUser.FARMER1.copy()
  val vet = TestUser.VET1.copy()
  val report1 = TestReport.REPORT1

  private fun setPlannerScreen(
      reportId: String? = null,
      user: User = farmer,
      goBack: () -> Unit = {},
      tabClicked: (Screen) -> Unit = {},
      reportClicked: (String) -> Unit = {},
  ) {
    setContent {
      PlannerScreen(
          user = user,
          reportId = reportId,
          goBack = goBack,
          tabClicked = tabClicked,
          reportClicked = reportClicked,
          plannerVM = PlannerViewModel(reportRepository),
      )
    }
  }

  override fun displayAllComponents() {}

  @Test
  fun interactionTest() = runTest {
    var goBackCalled = false
    var tabClickedCalledWith: Screen? = null
    var reportClickedCalledWith: String? = null

    val report = report1.copy(startTime = today.atTime(1, 0))

    runBlocking { reportRepository.addReport(report) }

    setPlannerScreen(
        goBack = { goBackCalled = true },
        tabClicked = { screen -> tabClickedCalledWith = screen },
        reportClicked = { reportId -> reportClickedCalledWith = reportId })

    with(NavigationTestTags) {
      assertFalse(goBackCalled)
      clickOn(GO_BACK_BUTTON)
      assertTrue(goBackCalled)

      assertNull(tabClickedCalledWith)
      nodeIsDisplayed(BOTTOM_NAVIGATION_MENU)
      clickOn(OVERVIEW_TAB)
      assertEquals(Screen.Overview, tabClickedCalledWith)
    }

    with(PlannerScreenTestTags) {
      assertNull(reportClickedCalledWith)
      scrollDailySchedulerToReportCardWithId(report)
      clickOn(reportCardTag(report.id))
      assertEquals(report.id, reportClickedCalledWith)
    }
  }

  @Test
  fun testUnchangedDateAlert() = runTest {
    var goBackCalled = false
    val report1 = report1.copy(startTime = null)

    runBlocking { reportRepository.addReport(report1) }

    setPlannerScreen(reportId = report1.id, user = vet, goBack = { goBackCalled = true })

    with(PlannerScreenTestTags) {
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeIsDisplayed(UNSAVED_ALERT_BOX)
      clickOn(UNSAVED_ALERT_BOX_CANCEL)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)
      assertFalse(goBackCalled)

      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeIsDisplayed(UNSAVED_ALERT_BOX)
      clickOn(UNSAVED_ALERT_BOX_GO_BACK)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)
      assertTrue(goBackCalled)
      goBackCalled = false

      clickOn(SET_REPORT_DATE_BUTTON)
      clickOn(NavigationTestTags.GO_BACK_BUTTON)
      nodeNotDisplayed(UNSAVED_ALERT_BOX)
      assertTrue(goBackCalled)
    }
  }

  @Test
  fun reportsForSelectedDate_areCorrectlyDisplayed_andDatesCorrectlyDisplayed() = runTest {
    val otherDay = if (today == tuesday) sunday else tuesday
    val user = farmer

    val testReport1 =
        report1.copy(id = "rep_id1", farmerId = user.uid, startTime = today.atTime(6, 0))
    val testReport2 =
        report1.copy(
            id = "rep_id2",
            farmerId = user.uid,
            startTime = today.atTime(10, 0),
            duration = LocalTime.of(1, 0))
    val testReport3 =
        report1.copy(
            id = "rep_id3",
            farmerId = user.uid,
            startTime = otherDay.atTime(14, 0),
            duration = LocalTime.of(3, 10))

    reportRepository.addReport(testReport1)
    reportRepository.addReport(testReport2)
    reportRepository.addReport(testReport3)

    setPlannerScreen(user = user)

    with(PlannerScreenTestTags) {
      checkDisplayedDateInfoIsCorrect(today)

      scrollDailySchedulerToReportCardWithId(testReport2)
      scrollDailySchedulerToReportCardWithId(testReport1)
      scrollDailySchedulerToReportCardWithId(testReport2)

      assertReportNotInDailyScheduler(testReport3)

      // Change selected date to Other Day
      clickOn(dayCardTag(otherDay))
      checkDisplayedDateInfoIsCorrect(otherDay)

      // Check reports for Other Day
      assertReportNotInDailyScheduler(testReport1)
      assertReportNotInDailyScheduler(testReport2)

      scrollDailySchedulerToReportCardWithId(testReport3)
    }
  }

  @Test
  fun plannerScreenWithReportIdWorksCorrectly() = runTest {
    val report = report1

    reportRepository.addReport(report)

    setPlannerScreen(reportId = report.id, user = vet)

    with(PlannerScreenTestTags) {
      nodeIsDisplayed(SET_REPORT_DATE_BOX)
      assertReportNotInDailyScheduler(report)

      clickOn(SET_REPORT_DATE_BUTTON)

      scrollDailySchedulerToReportCardWithId(report)
      nodeIsDisplayed(reportCardTag(report.id))
    }
  }

  @Test
  fun planner_loadReports_showsAndHidesLoadingOverlay() = runTest {
    val report = report1.copy(startTime = today.atTime(9, 0))
    val slowRepo = FakeReportRepository(initialReports = listOf(report), delayMs = SHORT_TIMEOUT)

    val slowVm = PlannerViewModel(reportRepository = slowRepo)

    setContent {
      PlannerScreen(
          user = farmer,
          reportId = null,
          goBack = {},
          tabClicked = {},
          reportClicked = {},
          plannerVM = slowVm,
      )
    }

    composeTestRule.assertOverlayDuringLoading(isLoading = { slowVm.uiState.value.isLoading })
  }

  @Test
  fun eventClusteringTest() {
    val layout1 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id2", status = ReportStatus.RESOLVED, duration = LocalTime.of(1, 0)),
            LocalTime.of(1, 0),
            LocalTime.of(2, 0))
    val layout2 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id3", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10)),
            LocalTime.of(1, 30),
            LocalTime.of(2, 0))
    val layout3 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id4", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10)),
            LocalTime.of(1, 0),
            LocalTime.of(1, 30))
    val layout4 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id4", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10)),
            LocalTime.of(3, 0),
            LocalTime.of(4, 0))
    val layout5 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id5", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10)),
            LocalTime.of(3, 0),
            LocalTime.of(4, 0))
    val layout6 =
        ReportLayoutItem(
            report1.copy(
                id = "rep_id6", status = ReportStatus.IN_PROGRESS, duration = LocalTime.of(3, 10)),
            LocalTime.of(5, 0),
            LocalTime.of(6, 0))

    val clusters = clusterEvents(listOf(layout1, layout2, layout3, layout4, layout5, layout6))

    assertEquals(3, clusters.size)
    assertEquals(3, clusters[0].size)
    assertEquals(2, clusters[1].size)
    assertEquals(1, clusters[2].size)

    for (c in clusters) {
      assignLanes(c)
    }
    assertEquals(2, layout1.totalLanes)
    assertNotEquals(layout1.lane, layout2.lane)
    assertEquals(layout2.lane, layout3.lane)
    assertEquals(2, layout4.totalLanes)
    assertEquals(2, layout5.totalLanes)
    assertNotEquals(layout4.lane, layout5.lane)
    assertEquals(1, layout6.totalLanes)
  }

  fun assertReportNotInDailyScheduler(report: Report) {
    assertThrows(AssertionError::class.java) { scrollDailySchedulerToReportCardWithId(report) }
  }

  fun scrollDailySchedulerToReportCardWithId(report: Report) =
      scrollTo(
          PlannerScreenTestTags.DAILY_SCHEDULER, PlannerScreenTestTags.reportCardTag(report.id))

  fun checkDisplayedDateInfoIsCorrect(today: LocalDate) {
    with(PlannerScreenTestTags) {
      textContains(
          WEEK_NUMBER, today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString(), ignoreCase = true)

      textContains(SELECTED_DATE, today.dayOfMonth.toString(), ignoreCase = true)
      textContains(SELECTED_DATE, today.month.name.take(3), ignoreCase = true)

      textContains(
          WEEK_HEADER, today.with(DayOfWeek.MONDAY).dayOfMonth.toString(), ignoreCase = true)
      textContains(
          WEEK_HEADER, today.with(DayOfWeek.SUNDAY).dayOfMonth.toString(), ignoreCase = true)

      (0..6)
          .map { today.with(DayOfWeek.MONDAY).plusDays(it.toLong()) }
          .forEach { date ->
            textContains(dayCardTag(date), date.dayOfMonth.toString(), ignoreCase = true)
          }
    }
  }
}
