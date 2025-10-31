package com.android.agrihealth.ui.overview

import androidx.credentials.CredentialManager
import com.android.agrihealth.data.model.user.UserRole
import kotlinx.coroutines.flow.StateFlow

interface OverviewViewModelContract {
  val uiState: StateFlow<OverviewUIState>

  fun loadReports(userRole: UserRole, userId: String)

  fun signOut(credentialManager: CredentialManager)
}
