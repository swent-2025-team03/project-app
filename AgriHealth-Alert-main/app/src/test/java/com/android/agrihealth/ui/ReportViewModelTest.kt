package com.android.agrihealth.ui

import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.ui.report.ReportViewModel
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
  private lateinit var viewModel: ReportViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    repository = FakeReportRepository()
    viewModel = ReportViewModel(repository)
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

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}

class FakeReportRepository : ReportRepository {
  val sampleReport =
      Report(
          id = "RPT001",
          title = "Test report",
          description = "desc",
          photoUri = null,
          farmerId = "F1",
          vetId = "V1",
          status = ReportStatus.PENDING,
          answer = "old answer",
          location = Location(0.0, 0.0, "Nowhere"))

  val savedReports = mutableListOf<Report>()
  var throwOnGet = false
  var throwOnSave = false

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

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> = emptyList()

  override suspend fun addReport(report: Report) {
    // not used
  }

  override suspend fun deleteReport(reportId: String) {
    // not used
  }

  override fun getNewReportId(): String = "FAKE_ID_123"
}
