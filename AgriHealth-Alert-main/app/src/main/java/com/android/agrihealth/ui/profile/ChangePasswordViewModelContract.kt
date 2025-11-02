package com.android.agrihealth.ui.profile

import kotlinx.coroutines.flow.StateFlow

interface ChangePasswordViewModelContract {

  val uiState: StateFlow<ChangePasswordUiState>

  fun setOldPassword(string: String)

  fun setNewPassword(string: String)

  fun setEmail(string: String)

  fun changePassword(): Boolean
}
