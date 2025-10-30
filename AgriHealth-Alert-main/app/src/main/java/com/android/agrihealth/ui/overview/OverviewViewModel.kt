package com.android.agrihealth.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.location.Location
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
)

/**
 * ViewModel holding the state of reports displayed on the Overview screen. Currently uses mock data
 * only. Repository integration will be added later.
 */
class OverviewViewModel(
    private val reportRepository: ReportRepository = ReportRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(OverviewUIState())
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

  /** Loads reports based on user role and ID. */
  fun loadReports(userRole: UserRole, userId: String) {
    viewModelScope.launch {
      try {
        val reports =
            when (userRole) {
              UserRole.FARMER -> reportRepository.getReportsByFarmer(userId)
              UserRole.VET -> reportRepository.getReportsByVet(userId)
            }
        _uiState.value = OverviewUIState(reports = reports)
      } catch (e: Exception) {
        _uiState.value = OverviewUIState(reports = emptyList())
      }
    }
  }

  /**
   * Is Now handled in when fetching the data from the repository
   *
   * Return reports filtered by user role. Farmers see only their own reports, Vets see all reports.
   */
  fun getReportsForUser(userRole: UserRole, userId: String): List<Report> {
    val allReports = uiState.value.reports

    return when (userRole) {
      UserRole.FARMER -> allReports.filter { it.farmerId == userId }
      UserRole.VET -> allReports
      else -> emptyList()
    }
  }
}
