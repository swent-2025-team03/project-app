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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUIState(
    override val email: String = "",
    override val password: String = "",
    override val user: FirebaseUser? = Firebase.auth.currentUser
) : SignInAndSignUpCommons

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
  fun signInWithEmailAndPassword() {
    if (_uiState.value.isValid()) {
      viewModelScope.launch {
        repository.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password).fold({
            user ->
          _uiState.update { it.copy(user = user) }
        }) { failure ->
        }
      }
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
        repository.signInWithGoogle(credential).fold({ user ->
          _uiState.update { it.copy(user = user) }
        }) { failure ->
          _uiState.update { it.copy(user = null) }
        }
      } catch (e: GetCredentialCancellationException) {
        // User cancelled the sign-in flow
        _uiState.update { it.copy(user = null) }
      } catch (e: androidx.credentials.exceptions.GetCredentialException) {
        // Other credential errors
        _uiState.update { it.copy(user = null) }
      } catch (e: Exception) {
        // Unexpected errors
        _uiState.update { it.copy(user = null) }
      }
    }
  }
}
