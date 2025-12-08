package com.android.agrihealth.testutil

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.getDistanceInsideZone
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.overview.AlertUiState
import com.android.agrihealth.ui.overview.AssignmentFilter
import com.android.agrihealth.ui.overview.OverviewUIState
import com.android.agrihealth.ui.overview.OverviewViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FakeOverviewViewModel(
    user: User? = null,
    alertRepository: FakeAlertRepository = FakeAlertRepository()
) : ViewModel(), OverviewViewModelContract {

  private val fakeReports =
      listOf(
          Report(
              id = "report_001",
              title = "Test Report",
              description = "This is a test report",
              questionForms = emptyList(),
              photoURL = null,
              farmerId = user?.uid ?: "dummy_farmer",
              officeId = "off_001",
              status = ReportStatus.PENDING,
              answer = null,
              location = null))

  private val _uiState =
      MutableStateFlow(
          OverviewUIState(
              reports = fakeReports,
              filteredReports = fakeReports,
              alerts = alertRepository.allAlerts,
              sortedAlerts =
                  alertRepository.allAlerts
                      .map { alert ->
                        val distance =
                            user?.address?.let { address -> alert.getDistanceInsideZone(address) }
                        AlertUiState(alert = alert, distanceToAddress = distance)
                      }
                      .sortedBy { it.distanceToAddress ?: Double.POSITIVE_INFINITY }))

  override val uiState: StateFlow<OverviewUIState> = _uiState

  override fun loadReports(user: User) {}

  override fun updateFiltersForReports(
      status: ReportStatus?,
      officeId: String?,
      farmerId: String?,
      assignment: AssignmentFilter?
  ) {}

  override fun loadAlerts(user: User) {}

  private lateinit var authRepository: AuthRepository

  override fun signOut(credentialManager: CredentialManager) {
    authRepository = AuthRepositoryProvider.repository
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
