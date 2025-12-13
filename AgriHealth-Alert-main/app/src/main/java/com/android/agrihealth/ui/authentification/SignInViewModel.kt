package com.android.agrihealth.ui.authentification

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.R
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.user.UserRepository
import com.android.agrihealth.data.model.user.UserRepositoryProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object SignInErrorMsg {
  const val EMPTY_EMAIL_OR_PASSWORD = "Please enter your email and password."
  const val INVALID_CREDENTIALS = "User not found with this email and password."
  const val TIMEOUT = "Not connected to the internet."
  const val UNEXPECTED = "Something unexpected happened, try again."
}

data class SignInUIState(
    val email: String = "",
    val password: String = "",
    val emailIsInvalid: Boolean = false,
    val passwordIsInvalid: Boolean = false,
    val verified: Boolean? = null,
    val isNewGoogle: Boolean = false,
    val errorMsg: String? = null,
) {
  val isValid: Boolean
    get() = email.isNotEmpty() && password.isNotEmpty()
}

class SignInViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

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
  fun signInWithEmailAndPassword() {
    if (_uiState.value.isValid) {
      viewModelScope.launch {
        authRepository
            .signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
            .fold({ verified -> _uiState.update { it.copy(verified = verified) } }) { failure ->
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

  private fun getSignInOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /** Initiates the Google sign-in flow and updates the UI state on success or failure. */
  fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {

    viewModelScope.launch {
      val signInOptions = getSignInOptions(context)
      val signInRequest = signInRequest(signInOptions)

      try {
        // Launch Credential Manager UI safely
        val credential = getCredential(context, signInRequest, credentialManager)

        // Pass the credential to your repository
        authRepository.signInWithGoogle(credential).fold({ uid ->
          if (userRepository.getUserFromId(uid).isFailure)
              _uiState.update { it.copy(verified = true, isNewGoogle = true) }
          else _uiState.update { it.copy(verified = true) }
        }) { failure ->
          _uiState.update { it.copy(verified = null, errorMsg = SignInErrorMsg.UNEXPECTED) }
        }
      } catch (e: GetCredentialCancellationException) {
        // User cancelled the sign-in flow
        _uiState.update { it.copy(verified = null) }
      } catch (e: Exception) {
        // Unexpected errors
        _uiState.update { it.copy(verified = null, errorMsg = SignInErrorMsg.UNEXPECTED) }
      }
    }
  }
}
