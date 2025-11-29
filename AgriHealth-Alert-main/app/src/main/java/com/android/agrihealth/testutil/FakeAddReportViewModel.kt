package com.android.agrihealth.testutil

import androidx.lifecycle.ViewModel
import com.android.agrihealth.data.model.report.MCQ
import com.android.agrihealth.data.model.report.MCQO
import com.android.agrihealth.data.model.report.OpenQuestion
import com.android.agrihealth.data.model.report.QuestionForm
import com.android.agrihealth.data.model.report.YesOrNoQuestion
import com.android.agrihealth.ui.report.AddReportUiState
import com.android.agrihealth.ui.report.AddReportViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAddReportViewModel : ViewModel(), AddReportViewModelContract {
  private val _uiState =
      MutableStateFlow(
          AddReportUiState(
              questionForms =
                  listOf(
                      OpenQuestion("Open Question 1", ""),
                      YesOrNoQuestion("Yes/No Question 1", 0),
                      MCQ("MCQ Question 1", listOf("Option A", "Option B"), -1),
                      MCQO("MCQO Question 1", listOf("Option A", "Option B"), -1, ""))))
  override val uiState: StateFlow<AddReportUiState> = _uiState

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
    return _uiState.value.title.isNotBlank() && _uiState.value.description.isNotBlank()
  }

  override fun clearInputs() {
    _uiState.value = AddReportUiState()
  }
}
