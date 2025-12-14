package com.android.agrihealth.ui.overview

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.alert.Alert
import com.android.agrihealth.data.model.alert.AlertRepository
import com.android.agrihealth.data.model.alert.AlertRepositoryProvider
import com.android.agrihealth.data.model.alert.getDistanceInsideZone
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.report.Report
import com.android.agrihealth.data.model.report.ReportRepository
import com.android.agrihealth.data.model.report.ReportRepositoryProvider
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertUiState(val alert: Alert, val distanceToAddress: Double? = null)

data class OverviewUIState(
    val reports: List<Report> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val selectedStatus: ReportStatus? = null,
    val selectedOffice: String? = null,
    val selectedFarmer: String? = null,
    val officeOptions: List<String> = emptyList(),
    val farmerOptions: List<String> = emptyList(),
    val filteredReports: List<Report> = emptyList(),
    val sortedAlerts: List<AlertUiState> = emptyList(),
    val selectedAssignmentFilter: AssignmentFilter? = null
)

enum class AssignmentFilter {
  ASSIGNED_TO_CURRENT_VET,
  UNASSIGNED,
  ASSIGNED_TO_OTHERS
}

sealed class FilterArg<out T> {
  object Unset : FilterArg<Nothing>()

  object Reset : FilterArg<Nothing>()

  data class Value<T>(val value: T?) : FilterArg<T>()
}

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

  private var currentUserId: String = ""

  /**
   * Loads reports for the user based on role and updates UIState. Applies selected filters to
   * generate filteredReports.
   */
  override fun loadReports(user: User) {
    viewModelScope.launch {
      try {
        val reports = reportRepository.getAllReports(user.uid).sortedByDescending { it.createdAt }
        val officeOptions = reports.map { it.officeId }.distinct()
        val farmerOptions = reports.map { it.farmerId }.distinct()
        val filtered =
            applyFiltersForReports(
                reports,
                _uiState.value.selectedStatus,
                _uiState.value.selectedOffice,
                _uiState.value.selectedFarmer,
                _uiState.value.selectedAssignmentFilter,
                currentUserId = user.uid)

        _uiState.value =
            _uiState.value.copy(
                reports = reports,
                officeOptions = officeOptions,
                farmerOptions = farmerOptions,
                filteredReports = filtered)

        currentUserId = user.uid
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(reports = emptyList())
      }
    }
  }

  /**
   * Updates the selected report filters in UIState and recalculates filteredReports.
   *
   * Each parameter is a FilterArg:
   * - FilterArg.Unset -> keep the current selection
   * - FilterArg.Value(v) -> set the selection to v (v may be null which clears the filter)
   */
  override fun updateFiltersForReports(
      status: FilterArg<ReportStatus>,
      officeId: FilterArg<String>,
      farmerId: FilterArg<String>,
      assignment: FilterArg<AssignmentFilter>
  ) {
    val current = _uiState.value
    val newStatus =
        when (status) {
          is FilterArg.Value -> status.value
          FilterArg.Unset -> current.selectedStatus
          FilterArg.Reset -> null
        }
    val newOffice =
        when (officeId) {
          is FilterArg.Value -> officeId.value
          FilterArg.Unset -> current.selectedOffice
          FilterArg.Reset -> null
        }
    val newFarmer =
        when (farmerId) {
          is FilterArg.Value -> farmerId.value
          FilterArg.Unset -> current.selectedFarmer
          FilterArg.Reset -> null
        }
    val newAssignment =
        when (assignment) {
          is FilterArg.Value -> assignment.value
          FilterArg.Unset -> current.selectedAssignmentFilter
          FilterArg.Reset -> null
        }

    val filtered =
        applyFiltersForReports(
            reports = current.reports,
            status = newStatus,
            officeId = newOffice,
            farmerId = newFarmer,
            assignment = newAssignment,
            currentUserId = currentUserId)

    _uiState.value =
        current.copy(
            selectedStatus = newStatus,
            selectedOffice = newOffice,
            selectedFarmer = newFarmer,
            selectedAssignmentFilter = newAssignment,
            filteredReports = filtered)
  }

  /**
   * Filters reports by optional status, office, and farmer. Only reports matching all non-null
   * filters are returned.
   */
  private fun applyFiltersForReports(
      reports: List<Report>,
      status: ReportStatus?,
      officeId: String?,
      farmerId: String?,
      assignment: AssignmentFilter?,
      currentUserId: String
  ): List<Report> {
    return reports.filter { report ->
      val matchesStatus = (status == null || report.status == status)
      val matchesOffice = (officeId == null || report.officeId == officeId)
      val matchesFarmer = (farmerId == null || report.farmerId == farmerId)

      val matchesAssignment =
          when (assignment) {
            null -> true
            AssignmentFilter.ASSIGNED_TO_CURRENT_VET -> report.assignedVet == currentUserId
            AssignmentFilter.UNASSIGNED -> report.assignedVet == null
            AssignmentFilter.ASSIGNED_TO_OTHERS ->
                report.assignedVet != null && report.assignedVet != currentUserId
          }

      matchesStatus && matchesOffice && matchesFarmer && matchesAssignment
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
   * Loads all alerts from the repository and updates UIState. Alerts are sorted based on the user's
   * location.
   */
  override fun loadAlerts(user: User) {
    viewModelScope.launch {
      try {
        val alerts = alertRepository.getAlerts()
        _uiState.value = _uiState.value.copy(alerts = alerts)
        updateSortedAlerts(user)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(alerts = emptyList(), sortedAlerts = emptyList())
      }
    }
  }

  /**
   * Sorts alerts by distance to the given user's address (closest first) and wraps each in an
   * AlertUiState. Alerts outside any zone or with null address will have distanceToAddress = null.
   */
  private fun updateSortedAlerts(user: User) {
    val address = user.address
    val sorted =
        _uiState.value.alerts
            .map { alert ->
              val distance = address?.let { alert.getDistanceInsideZone(it) }
              AlertUiState(alert = alert, distanceToAddress = distance)
            }
            .sortedBy { it.distanceToAddress ?: Double.POSITIVE_INFINITY }
    _uiState.value = _uiState.value.copy(sortedAlerts = sorted)
  }

  override fun signOut(credentialManager: CredentialManager) {
    authRepository = AuthRepositoryProvider.repository
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
