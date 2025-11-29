package com.android.agrihealth.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val chosenOffice: String = "",
    val isCollected: Boolean = false,
    val questionForms: List<QuestionForm> = emptyList(),
)

class AddReportViewModel(
    private val userId: String,
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository
) : ViewModel(), AddReportViewModelContract {
  private val _uiState = MutableStateFlow(AddReportUiState())
  override val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

  init {
    _uiState.value =
        _uiState.value.copy(questionForms = HealthQuestionFactory.animalHealthQuestions())
  }

    override fun switchIsCollected() {
        _uiState.value = _uiState.value.copy(isCollected = !uiState.value.isCollected)
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

  override fun updateQuestion(index: Int, updated: QuestionForm) {
    val updatedList = _uiState.value.questionForms.toMutableList()
    updatedList[index] = updated
    _uiState.value = _uiState.value.copy(questionForms = updatedList)
  }

  override suspend fun createReport(): Boolean {
    val uiState = _uiState.value
    if (uiState.title.isBlank() || uiState.description.isBlank()) {
      return false
    }
    val allQuestionsAnswered = uiState.questionForms.all { it.isValid() }
    if (!allQuestionsAnswered) {
      return false
    }

    val newReport =
        Report(
            id = reportRepository.getNewReportId(),
            title = uiState.title,
            description = uiState.description,
            questionForms = uiState.questionForms,
            photoUri = null, // currently unused
            farmerId = userId,
            officeId = uiState.chosenOffice,
            status = ReportStatus.PENDING,
            answer = null,
            location = Location(46.7990813, 6.6259961) // null // optional until implemented
            )

    viewModelScope.launch { reportRepository.addReport(newReport) }

    // Clears all the fields
    clearInputs() // TODO: Call only if addReport succeeds

    return true
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
