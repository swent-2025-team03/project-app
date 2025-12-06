package com.android.agrihealth.ui.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.images.ImageUIState
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.HealthQuestionFactory
import com.android.agrihealth.data.model.report.QuestionForm
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenOffice: String = "", // TODO: Should be a separate type, not a String!
    val photoUri: Uri? = null,
    val questionForms: List<QuestionForm> = emptyList(),
    val uploadedImagePath: String? = null,
)

/** Represents the result of creating a report */
sealed class CreateReportResult {
  /** The report has successfully been created */
  object Success : CreateReportResult()

  /** There is a validation error. For example a required field is missing a value */
  object ValidationError : CreateReportResult()

  /**
   * Uploading the report to the repository failed. Or uploading the photo to the image repository
   * failed
   */
  data class UploadError(val e: Throwable) : CreateReportResult()
}

/**
 * The view associated to the report creation screen.
 *
 * @param userId The ID of the user viewing this screen
 * @param reportRepository The repository containing the reports
 * @param imageViewModel The view model used to handle uploading/downloading photos
 * @see AddReportScreen
 */
class AddReportViewModel(
    private val userId: String,
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val imageViewModel: ImageViewModel = ImageViewModel(),
) : ViewModel(), AddReportViewModelContract {
  private val _uiState = MutableStateFlow(AddReportUiState())
  override val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

  init {
    _uiState.value =
        _uiState.value.copy(questionForms = HealthQuestionFactory.animalHealthQuestions())
  }

  override fun setTitle(newTitle: String) {
    _uiState.value = _uiState.value.copy(title = newTitle)
  }

  override fun setDescription(newDescription: String) {
    _uiState.value = _uiState.value.copy(description = newDescription)
  }

  override fun setOffice(officeId: String) {
    _uiState.value = _uiState.value.copy(chosenOffice = officeId)
  }

  override fun setPhoto(uri: Uri?) {
    _uiState.value = _uiState.value.copy(photoUri = uri)
  }

  override fun removePhoto() {
    _uiState.value = _uiState.value.copy(photoUri = null)
  }

  override fun setUploadedImagePath(path: String?) {
    _uiState.value = _uiState.value.copy(uploadedImagePath = path)
  }

  override fun updateQuestion(index: Int, updated: QuestionForm) {
    val updatedList = _uiState.value.questionForms.toMutableList()
    updatedList[index] = updated
    _uiState.value = _uiState.value.copy(questionForms = updatedList)
  }

  override suspend fun createReport(): CreateReportResult {
    val current = _uiState.value

    // Input validation (checking that all fields are completed
    // TODO More validation may be necessary (e.g forcing to have an office assigned, ...)
    if (current.title.isBlank() || current.description.isBlank()) {
      return CreateReportResult.ValidationError
    }
    if (!current.questionForms.all { it.isValid() }) {
      return CreateReportResult.ValidationError
    }

    var uploadedPath = current.uploadedImagePath

    // Photo upload (if a photo has been chosen)
    if (current.photoUri != null) {
      imageViewModel.upload(current.photoUri)

      val resultState =
          imageViewModel.uiState.first {
            it is ImageUIState.UploadSuccess || it is ImageUIState.Error
          }

      when (resultState) {
        is ImageUIState.UploadSuccess -> {
          uploadedPath = resultState.path
          _uiState.value = _uiState.value.copy(uploadedImagePath = resultState.path)
        }
        is ImageUIState.Error -> {
          return CreateReportResult.UploadError(resultState.e)
        }
        else -> {
          return CreateReportResult.UploadError(
              IllegalStateException(AddReportFeedbackTexts.UNKNOWN))
        }
      }
    }

    val newReport =
        Report(
            id = reportRepository.getNewReportId(),
            title = current.title,
            description = current.description,
            questionForms = current.questionForms,
            photoURL = uploadedPath,
            farmerId = userId,
            officeId = current.chosenOffice,
            status = ReportStatus.PENDING,
            answer = null,
            location =
                Location(
                    46.7990813,
                    6.6259961), // TODO Create way to select location automatically or manually on a
            // map
        )

    try {
      reportRepository.addReport(newReport)
      clearInputs()
      return CreateReportResult.Success
    } catch (e: Exception) {
      return CreateReportResult.UploadError(e)
    }
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
