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
import com.android.agrihealth.ui.loading.withLoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenOffice: String = "",
    val collected: Boolean = false,
    val address: Location? = null,
    val photoUri: Uri? = null,
    val questionForms: List<QuestionForm> = emptyList(),
    val uploadedImagePath: String? = null,
    val isLoading: Boolean = false,
)

/** Represents the result of creating a report */
sealed class CreateReportResult {
  object Success : CreateReportResult()

  object ValidationError : CreateReportResult()

  data class UploadError(val e: Throwable) : CreateReportResult()
}

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

  override fun switchCollected() {
    _uiState.value = _uiState.value.copy(collected = !uiState.value.collected)
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

  override fun setAddress(address: Location?) {
    _uiState.value = _uiState.value.copy(address = address)
  }

  override fun updateQuestion(index: Int, updated: QuestionForm) {
    val updatedList = _uiState.value.questionForms.toMutableList()
    updatedList[index] = updated
    _uiState.value = _uiState.value.copy(questionForms = updatedList)
  }

  override suspend fun createReport(): CreateReportResult {
    val uiState = _uiState.value

    if (uiState.title.isBlank() ||
        uiState.description.isBlank() ||
        uiState.chosenOffice.isBlank() ||
        uiState.address == null) {
      return CreateReportResult.ValidationError
    }
    if (!uiState.questionForms.all { it.isValid() }) {
      return CreateReportResult.ValidationError
    }

    val allQuestionsAnswered = uiState.questionForms.all { it.isValid() }
    if (!allQuestionsAnswered) {
      return CreateReportResult.ValidationError
    }

    var uploadedPath = uiState.uploadedImagePath

    var result: CreateReportResult =
        CreateReportResult.UploadError(IllegalStateException("Unknown error"))

    _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
      // Photo upload (if a photo has been chosen)
      if (uiState.photoUri != null) {
        imageViewModel.upload(uiState.photoUri)

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
            result = CreateReportResult.UploadError(resultState.e)
            return@withLoadingState
          }
          else -> {
            result =
                CreateReportResult.UploadError(
                    IllegalStateException(AddReportFeedbackTexts.UNKNOWN))
            return@withLoadingState
          }
        }
      }

      val newReport =
          Report(
              id = reportRepository.getNewReportId(),
              title = uiState.title,
              description = uiState.description,
              questionForms = uiState.questionForms,
              photoURL = uploadedPath,
              farmerId = userId,
              officeId = uiState.chosenOffice,
              status = ReportStatus.PENDING,
              answer = null,
              collected = uiState.collected,
              location = uiState.address)

      try {
        reportRepository.addReport(newReport)
        clearInputs()
        result = CreateReportResult.Success
      } catch (e: Exception) {
        result = CreateReportResult.UploadError(e)
      }
    }

    return result
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
