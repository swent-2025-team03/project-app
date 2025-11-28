package com.android.agrihealth.ui.report

import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.testutil.FakeReportRepository
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
    assertEquals("", state.chosenOffice)
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
    assertEquals("", viewModel.uiState.value.chosenOffice)
    assertNull(viewModel.uiState.value.photoUri)
  }

  @Test
  fun setVet_updatesVetOnly() {
    viewModel.setOffice("Vet")
    assertEquals("Vet", viewModel.uiState.value.chosenOffice)
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
  fun createReport_withEmptyFields_returnsValidationError() = runBlocking {
    val result = viewModel.createReport()
    assertEquals(result, CreateReportResult.ValidationError)
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

        viewModel.setOffice(AddReportConstants.officeOptions[0])
        val result = viewModel.createReport()
        advanceUntilIdle() // To avoid errors of synchronization which would make this test
        // non-deterministic
        assertEquals(result, CreateReportResult.Success)

        // Fields are cleared
        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertEquals("", state.chosenOffice)

        // Report is saved
        assertNotNull(repository.lastAddedReport)
        val addedReport = repository.lastAddedReport!!
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
        assertEquals(AddReportConstants.officeOptions[0], addedReport.officeId)
        assertEquals(ReportStatus.PENDING, addedReport.status)
      }

  @Test
  fun clearInputs_resetsAllFields() {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setOffice("O")
    viewModel.clearInputs()
    val state = viewModel.uiState.value
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.chosenOffice)
    assertEquals(null, state.photoUri)
  }
}
