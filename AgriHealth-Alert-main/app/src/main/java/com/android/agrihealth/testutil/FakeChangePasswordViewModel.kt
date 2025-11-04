package com.android.agrihealth.testutil

import androidx.lifecycle.ViewModel
import com.android.agrihealth.ui.profile.ChangePasswordUiState
import com.android.agrihealth.ui.profile.ChangePasswordViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeChangePasswordViewModel(val oldPassword: String = "password") :
    ViewModel(), ChangePasswordViewModelContract {
  private val _uiState = MutableStateFlow(ChangePasswordUiState())
  override val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

  override fun setOldPassword(string: String) {
    _uiState.value = _uiState.value.copy(oldPassword = string)
  }

  override fun setNewPassword(string: String) {
    _uiState.value = _uiState.value.copy(newPassword = string)
  }

  override fun setEmail(string: String) {
    _uiState.value = _uiState.value.copy(email = string)
  }

  override fun changePassword() {
    _uiState.value = _uiState.value.copy(newWeak = false, oldWrong = false)
    if (_uiState.value.isWeak()) {
      _uiState.value = _uiState.value.copy(newWeak = true)
    } else if (_uiState.value.oldPassword != oldPassword) {
      _uiState.value = _uiState.value.copy(oldWrong = true)
    } else _uiState.value = _uiState.value.copy(success = true)
  }
}
