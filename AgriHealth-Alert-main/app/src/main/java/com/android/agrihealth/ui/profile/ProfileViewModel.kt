package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userViewModel: UserViewModel,
    private val connectionRepository: ConnectionRepository = ConnectionRepository()
) : ViewModel() {

  private val _generatedCode = MutableStateFlow<String?>(null)
  val generatedCode: StateFlow<String?> = _generatedCode

  fun updateUser(updatedUser: User) {
    userViewModel.updateUser(updatedUser)
  }

  fun generateVetCode() {
    val vet = userViewModel.user.value as? Vet ?: return
    viewModelScope.launch {
      val result = connectionRepository.generateCode(vet.uid)
      result.fold(
          onSuccess = { code ->
            _generatedCode.value = code
            val updatedVet = vet.copy(validCodes = vet.validCodes + code)
            userViewModel.updateUser(updatedVet)
          },
          onFailure = { _generatedCode.value = "Error" })
    }
  }
}
