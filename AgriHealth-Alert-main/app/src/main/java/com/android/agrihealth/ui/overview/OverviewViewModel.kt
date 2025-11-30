package com.android.agrihealth.ui.overview

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertRepository
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.alert.containsUser
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
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
    val selectedOffice: String? = null,
    val selectedFarmer: String? = null,
    val officeOptions: List<String> = emptyList(),
    val farmerOptions: List<String> = emptyList(),
    val filteredReports: List<Report> = emptyList(),
    val filteredAlerts: List<Alert> = emptyList()
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

  /**
   * Loads reports for the user based on role and updates UIState. Applies selected filters to
   * generate filteredReports.
   */
  override fun loadReports(user: User) {
    viewModelScope.launch {
      try {
        val reports =
            when (user) {
              is Farmer -> reportRepository.getReportsByFarmer(user.uid)
              is Vet -> reportRepository.getReportsByOffice(user.officeId ?: "")
            }.sortedByDescending { it.createdAt }
        val officeOptions = reports.map { it.officeId }.distinct()
        val farmerOptions = reports.map { it.farmerId }.distinct()
        val filtered =
            applyFiltersForReports(
                reports,
                _uiState.value.selectedStatus,
                _uiState.value.selectedOffice,
                _uiState.value.selectedFarmer,
            )

        _uiState.value =
            _uiState.value.copy(
                reports = reports,
                officeOptions = officeOptions,
                farmerOptions = farmerOptions,
                filteredReports = filtered)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(reports = emptyList())
      }
    }
  }

  override fun updateFiltersForReports(
      status: ReportStatus?,
      officeId: String?,
      farmerId: String?
  ) {
    val filtered = applyFiltersForReports(_uiState.value.reports, status, officeId, farmerId)
    _uiState.value =
        _uiState.value.copy(
            selectedStatus = status,
            selectedOffice = officeId,
            selectedFarmer = farmerId,
            filteredReports = filtered)
  }

  private fun applyFiltersForReports(
      reports: List<Report>,
      status: ReportStatus?,
      officeId: String?,
      farmerId: String?
  ): List<Report> {
    return reports.filter { report ->
      (status == null || report.status == status) &&
          (officeId == null || report.officeId == officeId) &&
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

  /**
   * Loads all alerts from the repository and updates UIState. Also filters alerts based on the
   * current user's location.
   */
  override fun loadAlerts(user: User) {
    viewModelScope.launch {
      try {
        val alerts = alertRepository.getAlerts()
        _uiState.value = _uiState.value.copy(alerts = alerts)
        updateFilteredAlerts(user)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(alerts = emptyList(), filteredAlerts = emptyList())
      }
    }
  }

  fun updateFilteredAlerts(user: User) {
    val filtered = applyFiltersForAlerts(_uiState.value.alerts, user)
    _uiState.value = _uiState.value.copy(filteredAlerts = filtered)
  }

  private fun applyFiltersForAlerts(alerts: List<Alert>, user: User): List<Alert> {
    return alerts.filter { alert ->
      if (alert.zones.isNullOrEmpty()) {
        true
      } else {
        user.address?.let { address -> alert.containsUser(address.latitude, address.longitude) }
            ?: false
      }
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
