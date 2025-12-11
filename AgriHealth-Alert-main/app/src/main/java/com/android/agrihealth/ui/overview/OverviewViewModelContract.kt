package com.android.agrihealth.ui.overview

import androidx.credentials.CredentialManager
import com.android.agrihealth.data.model.report.ReportStatus
import com.android.agrihealth.data.model.user.User
import kotlinx.coroutines.flow.StateFlow

interface OverviewViewModelContract {
  val uiState: StateFlow<OverviewUIState>

  fun loadReports(user: User)

  fun updateFiltersForReports(
      status: FilterArg<ReportStatus> = FilterArg.Unset,
      officeId: FilterArg<String> = FilterArg.Unset,
      farmerId: FilterArg<String> = FilterArg.Unset,
      assignment: FilterArg<AssignmentFilter> = FilterArg.Unset
  )

  fun loadAlerts(user: User)

  fun signOut(credentialManager: CredentialManager)
}
