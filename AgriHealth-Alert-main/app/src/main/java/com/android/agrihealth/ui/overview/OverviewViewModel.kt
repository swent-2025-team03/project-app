package com.android.agrihealth.ui.overview

import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.*
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
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

// Mock data for testing
val report1 =
    Report(
        id = "RPT001",
        title = "Cow coughing",
        description = "Coughing and nasal discharge observed in the barn.",
        photoUri = null,
        farmerId = "FARMER_001",
        vetId = "VET_001",
        status = ReportStatus.IN_PROGRESS,
        answer = null,
        location = Location(46.5191, 6.5668, "Lausanne Farm"))
val report2 =
    Report(
        id = "RPT002",
        title = "Sheep lost appetite",
        description = "One sheep has not eaten for two days.",
        photoUri = null,
        farmerId = "FARMER_001",
        vetId = "VET_001",
        status = ReportStatus.PENDING,
        answer = null,
        location = Location(46.5210, 6.5650, "Vaud Farm"))

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

  private lateinit var authRepository: AuthRepository

  init {
    // ---Add some mock reports for testing---
    viewModelScope.launch {
      try {
        reportRepository.addReport(report1)
        reportRepository.addReport(report2)
      } catch (e: Exception) {
        Log.e("OverviewViewModel", "Error fetching reports", e)
      }
    }
    // ------

    getAllReports()
  }

  /** Fetches all reports from the repository and updates the UI state. */
  private fun getAllReports() {
    viewModelScope.launch {
      try {
        // TODO: Replace with actual user ID from authentication
        val reports = reportRepository.getAllReports("FARMER_001")
        _uiState.value = OverviewUIState(reports = reports)
      } catch (e: Exception) {
        Log.e("OverviewViewModel", "Error fetching reports", e)
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

  fun signOut(credentialManager: CredentialManager) {
    authRepository = AuthRepositoryProvider.repository
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
