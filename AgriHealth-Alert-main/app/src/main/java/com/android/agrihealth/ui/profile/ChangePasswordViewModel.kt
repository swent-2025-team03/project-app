package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.ui.loading.withLoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val email: String = "",
    val oldPassword: String = "",
    val success: Boolean = false,
    val oldWrong: Boolean = false,
    val newPassword: String = "",
    val newWeak: Boolean = false,
    val isLoading: Boolean = false
) {
  fun isWeak(): Boolean {
    return newPassword.length < 6
  }
}

class ChangePasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel(), ChangePasswordViewModelContract {
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
    }

    viewModelScope.launch {
      _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
        repository
            .reAuthenticate(_uiState.value.email, _uiState.value.oldPassword)
            .fold(
                onSuccess = {
                  repository
                      .changePassword(_uiState.value.newPassword)
                      .fold(
                          onSuccess = { _uiState.value = _uiState.value.copy(success = true) },
                          onFailure = {})
                },
                onFailure = { _uiState.value = _uiState.value.copy(oldWrong = true) })
      }
    }
  }
}
