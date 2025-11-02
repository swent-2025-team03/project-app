package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.user.Farmer
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
  private val _vetClaimMessage = MutableStateFlow<String?>(null)
  val vetClaimMessage: StateFlow<String?> = _vetClaimMessage

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
          onFailure = { e ->
            _generatedCode.value = "Error: ${e.message}"
            e.printStackTrace()
          })
    }
  }

  fun claimVetCode(code: String) {
    val farmer = userViewModel.user.value as? Farmer ?: return
    viewModelScope.launch {
      val result = connectionRepository.claimCode(code, farmer.uid)
      result.fold(
          onSuccess = { vetId ->
            // Update farmer: add vetId to linkedVets (avoid duplicates)
            val updatedLinkedVets = (farmer.linkedVets + vetId).distinct()
            val newDefaultVet = farmer.defaultVet ?: vetId
            val updatedFarmer =
                farmer.copy(linkedVets = updatedLinkedVets, defaultVet = newDefaultVet)
            userViewModel.updateUser(updatedFarmer)

            // Also update the vet so it knows the farmer is linked
            val currentUser = userViewModel.user.value
            if (currentUser is Vet && currentUser.uid == vetId) {
              val updatedVet =
                  currentUser.copy(
                      validCodes = currentUser.validCodes - code,
                      linkedFarmers = (currentUser.linkedFarmers + farmer.uid).distinct())
              userViewModel.updateUser(updatedVet)
            }

            _vetClaimMessage.value = "Vet successfully added!"
          },
          onFailure = { e ->
            // Show a short readable error message
            val msg =
                when {
                  e.message?.contains("expired", true) == true -> "Code expired."
                  e.message?.contains("used", true) == true -> "Code already used."
                  e.message?.contains("not found", true) == true -> "Invalid code."
                  else -> "Could not add vet: ${e.message}"
                }
            _vetClaimMessage.value = msg
          })
    }
  }
}
