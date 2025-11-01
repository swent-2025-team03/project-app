package com.android.agrihealth.ui.report

import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Tests created with generative AI */
class FakeReportRepository : ReportRepository {
  var storedReport: Report? = null

  override fun getNewReportId(): String = "fake-id"

  override suspend fun getAllReports(userId: String): List<Report> = emptyList()

  override suspend fun getReportsByFarmer(farmerId: String): List<Report> = emptyList()

  override suspend fun getReportsByVet(vetId: String): List<Report> = emptyList()

  override suspend fun getReportById(reportId: String): Report? = null

  override suspend fun addReport(report: Report) {
    storedReport = report
  }

  override suspend fun editReport(reportId: String, newReport: Report) {}

  override suspend fun deleteReport(reportId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddReportViewModelTest {

  private lateinit var repository: FakeReportRepository
  private lateinit var viewModel: AddReportViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(StandardTestDispatcher()) // Necessary for scheduling coroutines
    repository = FakeReportRepository()
    viewModel = AddReportViewModel(userId = "fake-user-id", reportRepository = repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initial_uiState_isEmpty() {
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.chosenVet)
  }

  @Test
  fun setTitle_updatesTitleOnly() {
    viewModel.setTitle("Hello")
    assertEquals("Hello", viewModel.uiState.value.title)
    assertEquals("", viewModel.uiState.value.description)
  }

  @Test
  fun setDescription_updatesDescriptionOnly() {
    viewModel.setDescription("Desc")
    assertEquals("Desc", viewModel.uiState.value.description)
    assertEquals("", viewModel.uiState.value.title)
  }

  @Test
  fun setVet_updatesVetOnly() {
    viewModel.setVet("Vet")
    assertEquals("Vet", viewModel.uiState.value.chosenVet)
  }

  @Test
  fun createReport_withEmptyFields_returnsFalse() = runBlocking {
    assertFalse(viewModel.createReport())
  }

  @Test
  fun createReport_reportIsAddedToRepository() =
      runTest(StandardTestDispatcher()) {
        viewModel.setTitle("Report")
        viewModel.setDescription("A description")
        viewModel.setVet(AddReportConstants.vetOptions[0])
        val result = viewModel.createReport()
        advanceUntilIdle() // To avoid errors of synchronization which would make this test
        // non-deterministic
        assertTrue(result)
        // Fields are cleared
        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertEquals("", state.chosenVet)
        // Report is saved
        assertNotNull(repository.storedReport)
        val addedReport = repository.storedReport!!
        assertEquals("Report", addedReport.title)
        assertEquals("A description", addedReport.description)
        assertEquals(AddReportConstants.vetOptions[0], addedReport.vetId)
        assertEquals(ReportStatus.PENDING, addedReport.status)
      }

  @Test
  fun clearInputs_resetsAllFields() {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setVet("V")
    viewModel.clearInputs()
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.chosenVet)
  }
}
