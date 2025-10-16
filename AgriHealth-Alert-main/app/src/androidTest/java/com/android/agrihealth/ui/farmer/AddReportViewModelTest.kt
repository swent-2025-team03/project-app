package com.android.agrihealth.ui.farmer

import com.android.agrihealth.data.model.Report
import com.android.agrihealth.data.model.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeReportRepository : ReportRepository {
  var addedReport: Report? = null

  override fun getNewReportId(): String = "fake-id"

  override suspend fun getAllReports(userId: String): List<Report> = emptyList()
  override suspend fun getReportsByFarmer(farmerId: String): List<Report> = emptyList()
  override suspend fun getReportById(reportId: String): Report? = null
  override suspend fun addReport(report: Report) { addedReport = report }
  override suspend fun editReport(reportId: String, newReport: Report) {}
  override suspend fun deleteReport(reportId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddReportViewModelTest {

  private lateinit var repository: FakeReportRepository
  private lateinit var viewModel: AddReportViewModel

  @Before
  fun setup() {
    repository = FakeReportRepository()
    viewModel = AddReportViewModel(repository)
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
  fun createReport_withEmptyFields_returnsFalse() {
    assertFalse(viewModel.createReport())
  }

  @Test
  fun createReport_withTitleAndDescription_returnsTrue_and_ClearsFields_andSaves() = runTest(StandardTestDispatcher()) {
    viewModel.setTitle("Report")
    viewModel.setDescription("A description")
    viewModel.setVet("Vet123")
    val result = viewModel.createReport()
    assertTrue(result)
    // Fields are cleared
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.chosenVet)
    // Report is saved
    assertNotNull(repository.addedReport)
    repository.addedReport!!.apply {
      assertEquals("Report", title)
      assertEquals("A description", description)
      assertEquals("Vet123", vetId)
      assertEquals(ReportStatus.PENDING, status)
    }
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
