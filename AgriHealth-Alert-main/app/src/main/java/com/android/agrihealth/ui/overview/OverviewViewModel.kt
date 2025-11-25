package com.android.agrihealth.ui.overview

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertRepository
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.repository.ReportRepository
import com.android.agrihealth.data.repository.ReportRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the Overview screen.
 *
 * @property reports A list of `Report` items to be displayed in the Overview screen. Defaults to an
 *   empty list if no items are available.
 */
data class OverviewUIState(
    val reports: List<Report> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val selectedStatus: ReportStatus? = null,
    val selectedVet: String? = null,
    val selectedFarmer: String? = null,
    val vetOptions: List<String> = emptyList(),
    val farmerOptions: List<String> = emptyList(),
    val filteredReports: List<Report> = emptyList()
)

/**
 * ViewModel holding the state of reports displayed on the Overview screen. Currently uses mock data
 * only. Repository integration will be added later.
 */
class OverviewViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
    private val alertRepository: AlertRepository = AlertRepositoryProvider.repository
) : ViewModel(), OverviewViewModelContract {

  private val _uiState = MutableStateFlow(OverviewUIState())
  override val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  private lateinit var authRepository: AuthRepository

  /** Loads reports based on user role and ID. */
  override fun loadReports(userRole: UserRole, userId: String) {
    viewModelScope.launch {
      try {
        val reports =
            when (userRole) {
              UserRole.FARMER -> reportRepository.getReportsByFarmer(userId)
              UserRole.VET -> reportRepository.getReportsByVet(userId)
            }.sortedByDescending { it.createdAt }
        val vetOptions = reports.map { it.vetId }.distinct()
        val farmerOptions = reports.map { it.farmerId }.distinct()
        val filtered =
            applyFilters(
                reports,
                _uiState.value.selectedStatus,
                _uiState.value.selectedVet,
                _uiState.value.selectedFarmer,
            )

        _uiState.value =
            _uiState.value.copy(
                reports = reports,
                vetOptions = vetOptions,
                farmerOptions = farmerOptions,
                filteredReports = filtered)
      } catch (e: Exception) {
        _uiState.value = OverviewUIState(reports = emptyList())
      }
    }
  }

  override fun loadAlerts() {
    viewModelScope.launch {
      val alerts = alertRepository.getAlerts()
      _uiState.value = _uiState.value.copy(alerts = alerts)
    }
  }

  override fun updateFilters(status: ReportStatus?, vetId: String?, farmerId: String?) {
    val filtered = applyFilters(_uiState.value.reports, status, vetId, farmerId)
    _uiState.value =
        _uiState.value.copy(
            selectedStatus = status,
            selectedVet = vetId,
            selectedFarmer = farmerId,
            filteredReports = filtered)
  }

  private fun applyFilters(
      reports: List<Report>,
      status: ReportStatus?,
      vetId: String?,
      farmerId: String?
  ): List<Report> {
    return reports.filter { report ->
      (status == null || report.status == status) &&
          (vetId == null || report.vetId == vetId) &&
          (farmerId == null || report.farmerId == farmerId)
    }
  }

  /**
   * Is Now handled in when fetching the data from the repository Return reports filtered by user
   * role. Farmers see only their own reports, Vets see all reports.
   */
  fun getReportsForUser(userRole: UserRole, userId: String): List<Report> {
    val allReports = uiState.value.reports
    return when (userRole) {
      UserRole.FARMER -> allReports.filter { it.farmerId == userId }
      UserRole.VET -> allReports
    }
  }

  override fun signOut(credentialManager: CredentialManager) {
    authRepository = AuthRepositoryProvider.repository
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
