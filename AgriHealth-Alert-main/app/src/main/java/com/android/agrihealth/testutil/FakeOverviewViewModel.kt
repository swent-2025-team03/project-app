package com.android.agrihealth.testutil

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.ui.overview.OverviewUIState
import com.android.agrihealth.ui.overview.OverviewViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FakeOverviewUiState(val reports: List<Report> = emptyList())

class FakeOverviewViewModel(initialState: OverviewUIState = OverviewUIState()) :
    ViewModel(), OverviewViewModelContract {
  private val _uiState = MutableStateFlow(initialState)
  override val uiState: StateFlow<OverviewUIState> = _uiState

  private lateinit var authRepository: AuthRepository

  override fun loadReports(userRole: UserRole, userId: String) {
    val dummyReports =
        listOf(
            Report(
                id = "1",
                title = "Test Report",
                description = "This is a test report",
                questionForms = emptyList(),
                photoUri = null,
                farmerId = userId,
                vetId = "vet_001",
                status = ReportStatus.PENDING,
                answer = null,
                location = null))
    _uiState.value = OverviewUIState(reports = dummyReports)
  }

    override fun loadAlerts(){}

  // updateFilters is not used for tests so it is remained empty
  override fun updateFilters(status: ReportStatus?, vetId: String?, farmerId: String?) {}

  override fun signOut(credentialManager: CredentialManager) {
    authRepository = AuthRepositoryProvider.repository
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
