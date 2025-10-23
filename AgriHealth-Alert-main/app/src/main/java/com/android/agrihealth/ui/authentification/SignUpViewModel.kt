package com.android.agrihealth.ui.authentification

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object SignUpErrorMsg {
  const val EMPTY_FIELDS = "Please fill every field."
  const val ROLE_NULL = "Please select a role."
  const val BAD_EMAIL_FORMAT = "Invalid email format."
  const val WEAK_PASSWORD = "Your password is too weak, try adding more characters."
  const val CNF_PASSWORD_DIFF = "The password and confirm password don't match."
  const val ALREADY_USED_EMAIL = "This email is already in use, try using an other one."
  const val TIMEOUT = "Not connected to the internet."
}

data class SignUpUIState(
    val email: String = "",
    val password: String = "",
    val user: FirebaseUser? = null,
    val name: String = "",
    val surname: String = "",
    val cnfPassword: String = "",
    val role: UserRole? = null,
    val errorMsg: String? = null,
    val hasFailed: Boolean = false,
) {

  fun isValid(): Boolean {
    return !emailIsMalformed() &&
        isFilled() &&
        !passwordIsWeak() &&
        password == cnfPassword &&
        role != null
  }

  fun passwordIsWeak(): Boolean {
    return password.length < 6
  }

  fun emailIsMalformed(): Boolean {
    return !Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }

  fun isFilled(): Boolean {
    return email.isNotEmpty() &&
        password.isNotEmpty() &&
        name.isNotEmpty() &&
        surname.isNotEmpty() &&
        cnfPassword.isNotEmpty()
  }
}

class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(SignUpUIState())
  val uiState: StateFlow<SignUpUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  fun setName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
  }

  fun setSurname(surname: String) {
    _uiState.value = _uiState.value.copy(surname = surname)
  }

  fun setPassword(password: String) {
    _uiState.value = _uiState.value.copy(password = password)
  }

  fun setCnfPassword(cnfPassword: String) {
    _uiState.value = _uiState.value.copy(cnfPassword = cnfPassword)
  }

  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
  }

  fun onSelected(role: UserRole) {
    _uiState.value = _uiState.value.copy(role = role)
  }

  fun signUp() {
    if (_uiState.value.isValid()) {
      viewModelScope.launch {
        authRepository
            .signUpWithEmailAndPassword(
                _uiState.value.email,
                _uiState.value.password,
                User(
                    "",
                    _uiState.value.name,
                    _uiState.value.surname,
                    _uiState.value.role!!,
                    _uiState.value.email))
            .fold({ user -> _uiState.update { it.copy(user = user) } }) { failure ->
              when (failure) {
                is FirebaseAuthException -> setErrorMsg(SignUpErrorMsg.ALREADY_USED_EMAIL)
                else -> setErrorMsg(SignUpErrorMsg.TIMEOUT)
              }
            }
      }
    } else {
      _uiState.value = _uiState.value.copy(hasFailed = true)
      val errorMsg =
          when {
            !_uiState.value.isFilled() -> SignUpErrorMsg.EMPTY_FIELDS
            _uiState.value.role == null -> SignUpErrorMsg.ROLE_NULL
            _uiState.value.emailIsMalformed() -> SignUpErrorMsg.BAD_EMAIL_FORMAT
            _uiState.value.passwordIsWeak() -> SignUpErrorMsg.WEAK_PASSWORD
            _uiState.value.password != _uiState.value.cnfPassword ->
                SignUpErrorMsg.CNF_PASSWORD_DIFF
            else -> null
          }
      setErrorMsg(errorMsg!!)
    }
  }
}
