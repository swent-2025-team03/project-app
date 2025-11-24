package com.android.agrihealth.ui.report

import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.utils.TestAssetUtils
import com.android.agrihealth.utils.TestAssetUtils.cleanupTestAssets
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
  fun cleanup() {
    cleanupTestAssets()
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
    assertNull(viewModel.uiState.value.photoUri)
  }

  @Test
  fun setTitle_updatesTitleOnly() {
    viewModel.setTitle("Hello")
    assertEquals("Hello", viewModel.uiState.value.title)
    assertEquals("", viewModel.uiState.value.description)
    assertNull(viewModel.uiState.value.photoUri)
  }

  @Test
  fun setDescription_updatesDescriptionOnly() {
    viewModel.setDescription("Desc")
    assertEquals("Desc", viewModel.uiState.value.description)
    assertEquals("", viewModel.uiState.value.title)
    assertEquals("", viewModel.uiState.value.chosenVet)
    assertNull(viewModel.uiState.value.photoUri)
  }

  @Test
  fun setVet_updatesVetOnly() {
    viewModel.setVet("Vet")
    assertEquals("Vet", viewModel.uiState.value.chosenVet)
    assertEquals("", viewModel.uiState.value.title)
    assertEquals("", viewModel.uiState.value.description)
  }

  @Test
  fun setPhoto_updatesPhotoOnly() {
    val fakePicture = TestAssetUtils.getUriFrom(TestAssetUtils.FAKE_PHOTO_FILE)
    viewModel.setPhoto(fakePicture)
    assertEquals(fakePicture, viewModel.uiState.value.photoUri)
    assertEquals("", viewModel.uiState.value.title)
    assertEquals("", viewModel.uiState.value.description)
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

        val questions = viewModel.uiState.value.questionForms
        questions.forEachIndexed { index, question ->
          when (question) {
            is OpenQuestion -> {
              viewModel.updateQuestion(index, OpenQuestion(question.question, "Answer $index"))
            }
            is YesOrNoQuestion -> {
              viewModel.updateQuestion(index, YesOrNoQuestion(question.question, 0))
            }
            is MCQ -> {
              viewModel.updateQuestion(index, MCQ(question.question, question.answers, 0))
            }
            is MCQO -> {
              viewModel.updateQuestion(
                  index, MCQO(question.question, question.answers, 0, "Other answer $index"))
            }
          }
        }

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
        addedReport.questionForms.forEachIndexed { index, question ->
          when (question) {
            is OpenQuestion -> assertEquals("Answer $index", question.userAnswer)
            is YesOrNoQuestion -> assertEquals(0, question.answerIndex)
            is MCQ -> assertEquals(0, question.answerIndex)
            is MCQO -> {
              assertEquals(0, question.answerIndex)
              assertEquals("Other answer $index", question.userAnswer)
            }
          }
        }
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
    assertEquals(null, state.photoUri)
  }
}
