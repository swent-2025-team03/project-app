package com.android.agrihealth.ui.authentification

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val emailIsMalformed: Boolean = false,
    val emailSendStatus: EmailSendStatus = EmailSendStatus.None
)

/** ViewModel to link to ResetPasswordScreen and an AuthRepository */
class ResetPasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ResetPasswordUiState())
  val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
  }

  fun setEmailIsMalformed(value: Boolean) {
    _uiState.value = _uiState.value.copy(emailIsMalformed = value)
  }

  fun setEmailSendStatus(status: EmailSendStatus) {
    _uiState.value = _uiState.value.copy(emailSendStatus = status)
  }

  fun sendFormButtonClicked() {
    val malformed = emailIsMalformed()
    setEmailIsMalformed(malformed)
    if (!malformed) {
      setEmailSendStatus(EmailSendStatus.Waiting)
      viewModelScope.launch {
        repository
            .sendResetPasswordEmail(_uiState.value.email)
            .fold(
                onSuccess = { setEmailSendStatus(EmailSendStatus.Success) },
                onFailure = {
                  setEmailSendStatus(EmailSendStatus.Fail)
                  Log.e("ResetPasswordScreen", "Error sending reset password email", it)
                })
      }
    } else {
      setEmailSendStatus(EmailSendStatus.None)
    }
  }

  fun emailIsMalformed(): Boolean {
    return !Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()
  }
}

sealed class EmailSendStatus {
  object Success : EmailSendStatus()

  object Fail : EmailSendStatus()

  object Waiting : EmailSendStatus()

  object None : EmailSendStatus()
}
