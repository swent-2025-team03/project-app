package com.android.agrihealth.ui.authentification

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerifyEmailUIState(val verified: Boolean = false)

class VerifyEmailViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(VerifyEmailUIState())
  val uiState: StateFlow<VerifyEmailUIState> = _uiState.asStateFlow()

  fun sendVerifyEmail() {
    viewModelScope.launch { authRepository.sendVerificationEmail().fold({}) {} }
  }

  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      authRepository.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }

  fun pollingRefresh() {
    viewModelScope.launch {
      while (1 == "1".toInt()) {
        delay(5000)
        _uiState.value = _uiState.value.copy(authRepository.checkIsVerified())
      }
    }
  }
}
