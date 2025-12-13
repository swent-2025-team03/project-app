package com.android.agrihealth.ui

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.ReportRepository
import com.android.agrihealth.ui.report.ReportViewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

  private lateinit var repository: FakeReportRepository
  private lateinit var viewModel: ReportViewViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    repository = FakeReportRepository()
    viewModel = ReportViewViewModel(repository)
    Dispatchers.setMain(testDispatcher)
  }

  @Test
  fun `loadReport updates state when report exists`() = runTest {
    val expected = repository.sampleReport
    viewModel.loadReport(expected.id)
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals(expected.id, state.report.id)
    assertEquals(expected.status, state.status)
  }

  @Test
  fun `loadReport does not crash when report not found`() = runTest {
    viewModel.loadReport("UNKNOWN")
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertEquals("RPT001", state.report.id) // Default unchanged
  }

  @Test
  fun `loadReport handles exception safely`() = runTest {
    repository.throwOnGet = true
    viewModel.loadReport("ANY")
    advanceUntilIdle()
    assertEquals("RPT001", viewModel.uiState.value.report.id)
  }

  @Test
  fun `onAnswerChange updates answer text`() {
    viewModel.onAnswerChange("New answer")
    assertEquals("New answer", viewModel.uiState.value.answerText)
  }

  @Test
  fun `onStatusChange updates status`() {
    viewModel.onStatusChange(ReportStatus.RESOLVED)
    assertEquals(ReportStatus.RESOLVED, viewModel.uiState.value.status)
  }

  @Test
  fun `onSpam sets status to SPAM`() {
    viewModel.onSpam()
    assertEquals(ReportStatus.SPAM, viewModel.uiState.value.status)
  }

  @Test
  fun `onDelete deletes form repository`() = runTest {
    repository.lastDeleted = "test"
    viewModel.loadReport(repository.sampleReport.id)
    advanceUntilIdle()
    viewModel.onDelete()
    advanceUntilIdle()
    assertEquals(repository.sampleReport.id, repository.lastDeleted)
  }

  @Test
  fun `onSave calls repository with updated report`() = runTest {
    viewModel.onAnswerChange("Updated answer")
    viewModel.onStatusChange(ReportStatus.RESOLVED)
    viewModel.onSave()
    advanceUntilIdle()
    assertEquals(1, repository.savedReports.size)
    val saved = repository.savedReports.first()
    assertEquals("Updated answer", saved.answer)
    assertEquals(ReportStatus.RESOLVED, saved.status)
  }

  @Test
  fun `onSave handles repository exception gracefully`() = runTest {
    repository.throwOnSave = true
    viewModel.onSave()
    advanceUntilIdle() // should not crash
  }

  @Test
  fun `assignReportToVet assigns when none assigned`() = runTest {
    repository.sampleReport = repository.sampleReport.copy(assignedVet = null)

    viewModel.loadReport(repository.sampleReport.id)
    advanceUntilIdle()

    viewModel.assignReportToVet("VET_123")
    advanceUntilIdle()

    assertEquals("VET_123", repository.sampleReport.assignedVet)
    assertEquals("VET_123", viewModel.uiState.value.report.assignedVet)
  }

  @Test
  fun `assignReportToVet does nothing when already assigned`() = runTest {
    repository.sampleReport = repository.sampleReport.copy(assignedVet = "EXISTING")

    viewModel.loadReport(repository.sampleReport.id)
    advanceUntilIdle()

    viewModel.assignReportToVet("NEW_VET")
    advanceUntilIdle()

    // remains unchanged
    assertEquals("EXISTING", repository.sampleReport.assignedVet)
    assertEquals("EXISTING", viewModel.uiState.value.report.assignedVet)
  }

  @Test
  fun `unassign clears assignedVet when answer is empty`() = runTest {
    repository.sampleReport =
        repository.sampleReport.copy(
            assignedVet = "VET_777", answer = "" // empty answer → allowed
            )

    viewModel.loadReport(repository.sampleReport.id)
    advanceUntilIdle()

    viewModel.unassign()
    advanceUntilIdle()

    assertNull(repository.sampleReport.assignedVet)
    assertNull(viewModel.uiState.value.report.assignedVet)
  }

  @Test
  fun `unassign does nothing when answer is not empty`() = runTest {
    repository.sampleReport =
        repository.sampleReport.copy(assignedVet = "VET_777", answer = "Already answered")

    viewModel.loadReport(repository.sampleReport.id)
    advanceUntilIdle()

    viewModel.unassign()
    advanceUntilIdle()

    // Should NOT change
    assertEquals("VET_777", repository.sampleReport.assignedVet)
    assertEquals("VET_777", viewModel.uiState.value.report.assignedVet)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}

class FakeReportRepository : ReportRepository {
  var sampleReport =
      Report(
          id = "RPT001",
          title = "Test report",
          description = "desc",
          questionForms = emptyList(),
          photoURL = null,
          farmerId = "F1",
          officeId = "OFF1",
          status = ReportStatus.PENDING,
          answer = "old answer",
          location = Location(0.0, 0.0, "Nowhere"),
          assignedVet = null)

  val savedReports = mutableListOf<Report>()
  var throwOnGet = false
  var throwOnSave = false
  var lastDeleted = "lastDeletedReportId"

  override suspend fun getReportById(id: String): Report? {
    if (throwOnGet) throw RuntimeException("Test error")
    return if (id == sampleReport.id) sampleReport else null
  }

  override suspend fun editReport(id: String, report: Report) {
    if (throwOnSave) throw RuntimeException("Save error")
    savedReports.add(report)
  }

  // --- Unused methods for this ViewModel test ---
  override suspend fun getAllReports(userId: String): List<Report> = emptyList()

  override suspend fun addReport(report: Report) {
    // not used
  }

  override suspend fun deleteReport(reportId: String) {
    lastDeleted = reportId
  }

  override suspend fun assignReportToVet(reportId: String, vetId: String) {
    sampleReport = sampleReport.copy(assignedVet = vetId)
  }

  override suspend fun unassignReport(reportId: String) {
    sampleReport = sampleReport.copy(assignedVet = null)
  }

  override fun getNewReportId(): String = "FAKE_ID_123"
}
