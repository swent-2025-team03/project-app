package com.android.agrihealth.ui.authentification

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.loading.withLoadingState
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
    val uid: String? = null,
    val firstname: String = "",
    val lastname: String = "",
    val cnfPassword: String = "",
    val role: UserRole? = null,
    val errorMsg: String? = null,
    val hasFailed: Boolean = false,
    val isLoading: Boolean = false,
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
        firstname.isNotEmpty() &&
        lastname.isNotEmpty() &&
        cnfPassword.isNotEmpty()
  }
}

open class SignUpViewModel(
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
    _uiState.value = _uiState.value.copy(firstname = name)
  }

  fun setSurname(surname: String) {
    _uiState.value = _uiState.value.copy(lastname = surname)
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
    setEmail(_uiState.value.email.trim())
    setPassword(_uiState.value.password.trim())
    setCnfPassword(_uiState.value.cnfPassword.trim())
    val state = _uiState.value

    if (state.isValid()) {
      val user =
          when (state.role) {
            UserRole.FARMER ->
                Farmer("", state.firstname, state.lastname, state.email, null, emptyList(), null)
            UserRole.VET -> Vet("", state.firstname, state.lastname, state.email, null)
            else -> null
          }

      if (user != null) {
        viewModelScope.launch {
          _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
            authRepository
                .signUpWithEmailAndPassword(state.email, state.password, user)
                .fold(
                    { uid -> _uiState.update { it.copy(uid = uid) } },
                    { failure ->
                      setErrorMsg(
                          when (failure) {
                            is com.google.firebase.auth.FirebaseAuthException ->
                                SignUpErrorMsg.ALREADY_USED_EMAIL
                            else -> SignUpErrorMsg.TIMEOUT
                          })
                    })
          }
        }
      }
    } else {
      _uiState.value = state.copy(hasFailed = true)
      val errorMsg =
          when {
            !state.isFilled() -> SignUpErrorMsg.EMPTY_FIELDS
            state.role == null -> SignUpErrorMsg.ROLE_NULL
            state.emailIsMalformed() -> SignUpErrorMsg.BAD_EMAIL_FORMAT
            state.passwordIsWeak() -> SignUpErrorMsg.WEAK_PASSWORD
            state.password != state.cnfPassword -> SignUpErrorMsg.CNF_PASSWORD_DIFF
            else -> null
          }
      errorMsg?.let { setErrorMsg(it) }
    }
  }

  fun createLocalUser(uid: String): User? {
    val state = _uiState.value
    return when (state.role) {
      UserRole.FARMER ->
          Farmer(
              uid = uid,
              firstname = state.firstname,
              lastname = state.lastname,
              email = state.email,
              address = null,
              linkedOffices = emptyList(),
              defaultOffice = null)
      UserRole.VET ->
          Vet(
              uid = uid,
              firstname = state.firstname,
              lastname = state.lastname,
              email = state.email,
              address = null)
      else -> null
    }
  }
}
