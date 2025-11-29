package com.android.agrihealth.testutil

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertZone
import com.android.agrihealth.data.model.alert.containsUser
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.overview.OverviewUIState
import com.android.agrihealth.ui.overview.OverviewViewModelContract
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FakeOverviewUiState(val reports: List<Report> = emptyList())

class FakeOverviewViewModel(user: User? = null, initialState: OverviewUIState = OverviewUIState()) :
    ViewModel(), OverviewViewModelContract {
  private val fakeAlerts =
      listOf(
          Alert(
              id = "1",
              title = "Heatwave Warning",
              description = "High temperatures expected",
              outbreakDate = LocalDate.of(2025, 11, 22),
              region = "Vaud, Switzerland",
              zones = listOf(AlertZone(Location(46.5191, 6.5668, "EPFL"), 10000.0))),
          Alert(
              id = "2",
              title = "Drought Risk",
              description = "Low rainfall expected",
              outbreakDate = LocalDate.of(2025, 11, 22),
              region = "Vaud, Switzerland",
              zones = emptyList()))

  private val fakeReports =
      listOf(
          Report(
              id = "report_001",
              title = "Test Report",
              description = "This is a test report",
              questionForms = emptyList(),
              photoUri = null,
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
              alerts = fakeAlerts,
              filteredAlerts =
                  fakeAlerts.filter { alert ->
                    user?.address?.let { address ->
                      alert.containsUser(address.latitude, address.longitude)
                    } ?: false
                  }))

  override val uiState: StateFlow<OverviewUIState> = _uiState

  override fun loadReports(user: User) {}

  override fun updateFiltersForReports(
      status: ReportStatus?,
      officeId: String?,
      farmerId: String?
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
