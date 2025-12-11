package com.android.agrihealth.ui.authentification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object VerifyEmailErrorMsg {
  const val FAIL = "Something went wrong, make sure you are connected to the internet."
}

data class VerifyEmailUIState(
    val verified: Boolean = false,
    val errorMsg: String? = null,
    val enabled: Boolean = false
)

class VerifyEmailViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(VerifyEmailUIState())
  val uiState: StateFlow<VerifyEmailUIState> = _uiState.asStateFlow()

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  fun sendVerifyEmail() {
    viewModelScope.launch {
      try {
        authRepository.sendVerificationEmail().fold({
          _uiState.value = _uiState.value.copy(enabled = false)
        }) {
          _uiState.value = _uiState.value.copy(errorMsg = VerifyEmailErrorMsg.FAIL)
          _uiState.value = _uiState.value.copy(enabled = true)
        }
        delay(30_000)
        _uiState.value = _uiState.value.copy(enabled = true)
      } catch (_: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = VerifyEmailErrorMsg.FAIL)
      }
    }
  }

  fun pollingRefresh() {
    viewModelScope.launch {
      try {
        while (1 == "1".toInt()) {
          delay(5000)
          _uiState.value = _uiState.value.copy(verified = authRepository.checkIsVerified())
        }
      } catch (_: Exception) {
        /*don't make a pop up appear every 5 seconds*/
      }
    }
  }
}
