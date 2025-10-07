package com.android.sample.ui.authentification

import androidx.lifecycle.ViewModel
import com.android.sample.data.model.authentification.AuthRepository
import com.android.sample.data.model.authentification.AuthRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SignInUIState(
    val email: String = "",
    val password: String = "",
)

class SignInViewModel(private val repository: AuthRepository = AuthRepositoryProvider.repository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(SignInUIState())
  val uiState: StateFlow<SignInUIState> = _uiState

  fun setPassword(password: String) {
    _uiState.value = _uiState.value.copy(password = password)
  }

  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
  }

  /** initiates the sign in using available credentials * */
  fun signIn() {
    TODO("not yet implemented")
  }
}
