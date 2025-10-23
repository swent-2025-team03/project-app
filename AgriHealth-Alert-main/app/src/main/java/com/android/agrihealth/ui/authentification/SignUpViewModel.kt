package com.android.agrihealth.ui.authentification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.AuthRepository
import com.android.agrihealth.data.model.authentification.AuthRepositoryProvider
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserRole
import com.android.agrihealth.data.model.user.Vet
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUIState(
    override val email: String = "",
    override val password: String = "",
    override val user: FirebaseUser? = null,
    val firstname: String = "",
    val lastname: String = "",
    val cnfPassword: String = "",
    val role: UserRole? = null,
) : SignInAndSignUpCommons {

  override fun isValid(): Boolean {
    return super.isValid() && password == cnfPassword && role != null
  }
}

class SignUpViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(SignUpUIState())
  val uiState: StateFlow<SignUpUIState> = _uiState

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

  private fun createUser(user: User) {
    viewModelScope.launch { userRepository.addUser(user) }
  }

  fun signUp() {
    if (_uiState.value.isValid()) {
      if (_uiState.value.role == UserRole.FARMER) {
        viewModelScope.launch {
          authRepository
              .signUpWithEmailAndPassword(
                  _uiState.value.email,
                  _uiState.value.password,
                  Farmer(
                      "",
                      _uiState.value.firstname,
                      _uiState.value.lastname,
                      _uiState.value.email,
                      null,
                      emptyList(),
                      null))
              .fold({ user -> _uiState.update { it.copy(user = user) } }) { failure -> }
        }
      } else if (_uiState.value.role == UserRole.VET) {
        viewModelScope.launch {
          authRepository
              .signUpWithEmailAndPassword(
                  _uiState.value.email,
                  _uiState.value.password,
                  Vet(
                      "",
                      _uiState.value.firstname,
                      _uiState.value.lastname,
                      _uiState.value.email,
                      null))
              .fold({ user -> _uiState.update { it.copy(user = user) } }) { failure -> }
        }
      }
    }
  }
}
