package com.android.agrihealth.ui.authentification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object SignInErrorMsg {
  const val EMPTY_EMAIL_OR_PASSWORD = "Please enter your email and password."
  const val INVALID_CREDENTIALS = "User not found with this email and password."
  const val TIMEOUT = "Not connected to the internet."
}

data class SignInUIState(
    val email: String = "",
    val password: String = "",
    val emailIsInvalid: Boolean = false,
    val passwordIsInvalid: Boolean = false,
    val user: FirebaseUser? = Firebase.auth.currentUser,
    val errorMsg: String? = null,
) {
  val isValid: Boolean
    get() = email.isNotEmpty() && password.isNotEmpty()
}

class SignInViewModel(private val repository: AuthRepository = AuthRepositoryProvider.repository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(SignInUIState())
  val uiState: StateFlow<SignInUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  fun setPassword(password: String) {
    _uiState.value = _uiState.value.copy(password = password)
  }

  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
  }

  /** initiates the sign in using available credentials * */
  fun signIn() {
    if (_uiState.value.isValid) {
      viewModelScope.launch {
        repository.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password).fold({
            user ->
          _uiState.update { it.copy(user = user) }
        }) { failure ->
          when (failure) {
            is FirebaseAuthException -> setErrorMsg(SignInErrorMsg.INVALID_CREDENTIALS)
            else -> setErrorMsg(SignInErrorMsg.TIMEOUT)
          }
        }
      }
    } else {
      if (_uiState.value.email.isEmpty()) {
        _uiState.value = _uiState.value.copy(emailIsInvalid = true)
      }
      if (_uiState.value.password.isEmpty()) {
        _uiState.value = _uiState.value.copy(passwordIsInvalid = true)
      }
      setErrorMsg(SignInErrorMsg.EMPTY_EMAIL_OR_PASSWORD)
    }
  }
}
