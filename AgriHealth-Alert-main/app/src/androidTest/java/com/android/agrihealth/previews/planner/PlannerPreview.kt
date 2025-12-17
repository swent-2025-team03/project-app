package com.android.agrihealth.previews.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.agrihealth.core.design.theme.AgriHealthAppTheme
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.testhelpers.fakes.FakeReportRepository
import com.android.agrihealth.ui.planner.PlannerScreen
import com.android.agrihealth.ui.planner.PlannerViewModel
import java.time.LocalDate
import java.time.LocalTime

@Preview
@Composable
fun PlannerScreenPreview() {
  val startOfDay = LocalDate.now().atTime(0, 0)
  val previewReport1 =
      Report(
          id = "1",
          title = "Checkup with Farmer John",
          description = "Regular health checkup for the cattle.",
          questionForms = emptyList(),
          photoURL = null,
          farmerId = "farmer1",
          officeId = "vet1",
          status = ReportStatus.IN_PROGRESS,
          answer = null,
          location = Location(latitude = 12.34, longitude = 56.78, name = "Farmhouse A"),
          startTime = startOfDay.plusHours(2).plusMinutes(30),
          duration = LocalTime.of(0, 0),
          assignedVet = "vet1")

  val previewReport2 =
      previewReport1.copy(
          id = "2", startTime = startOfDay.plusHours(2), duration = LocalTime.of(1, 0))
  val previewReport3 =
      previewReport1.copy(
          id = "3",
          startTime = startOfDay.plusHours(2),
          duration = LocalTime.of(2, 0),
          status = ReportStatus.RESOLVED)
  val previewReport4 =
      previewReport1.copy(
          id = "4",
          status = ReportStatus.PENDING,
          startTime = startOfDay.plusHours(3).plusMinutes(45),
          duration = LocalTime.of(1, 30))

  val previewReport5 =
      previewReport1.copy(
          id = "5",
          startTime = startOfDay.plusHours(5).plusMinutes(45),
          duration = LocalTime.of(3, 0))

  val user =
      Vet(
          uid = "vet1",
          firstname = "test",
          lastname = "test",
          email = "test",
          address = null,
          officeId = "vet1",
          isGoogleAccount = false,
          description = "test",
          collected = false)

  val fakeReportRepository =
      FakeReportRepository(
          initialReports =
              listOf(
                  previewReport1, previewReport2, previewReport3, previewReport4, previewReport5))
  val fakePlannerVM = PlannerViewModel(fakeReportRepository)

  AgriHealthAppTheme { PlannerScreen(user = user, reportId = null, plannerVM = fakePlannerVM) }
}
