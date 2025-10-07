package com.android.sample.ui.authentification

import com.android.sample.data.model.authentification.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SignUpUIState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val cnfPassword: String = "",
    val role: UserRole? = null,
) {
  val isValid: Boolean
    get() = true
}

class SignUpViewModel {
  private val _uiState = MutableStateFlow(SignUpUIState())
  val uiState: StateFlow<SignUpUIState> = _uiState

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

  private fun createUser() {
    TODO("not yet implemented")
  }

  fun signUp() {
    TODO("not yet implemented")
  }
}
