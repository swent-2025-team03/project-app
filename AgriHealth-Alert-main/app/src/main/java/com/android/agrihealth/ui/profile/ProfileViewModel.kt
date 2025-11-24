package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.lang.IllegalStateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userViewModel: UserViewModel,
    private val connectionRepository: ConnectionRepository,
    private val officeRepository: OfficeRepository = OfficeRepositoryProvider.get()
) : ViewModel() {

  private val _generatedCode = MutableStateFlow<String?>(null)
  private val _vetClaimMessage = MutableStateFlow<String?>(null)
  val vetClaimMessage: StateFlow<String?> = _vetClaimMessage

  val generatedCode: StateFlow<String?> = _generatedCode

  fun updateUser(updatedUser: User) {
    userViewModel.updateUser(updatedUser)
  }

  fun generateVetCode() {
    val currentUser = userViewModel.user.value
    val vet = currentUser as? Vet ?: return

    viewModelScope.launch {
      val result = connectionRepository.generateCode()
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
    val user = userViewModel.user.value
    viewModelScope.launch {
      val result = connectionRepository.claimCode(code)
      result.fold(
          onSuccess = { vetId ->
            when (user) {
              is Farmer -> { // Update farmer: add vetId to linkedVets (avoid duplicates)
                val updatedLinkedVets = (user.linkedVets + vetId).distinct()
                val newDefaultVet = user.defaultVet ?: vetId
                val updatedFarmer =
                    user.copy(linkedVets = updatedLinkedVets, defaultVet = newDefaultVet)
                userViewModel.updateUser(updatedFarmer)

                _vetClaimMessage.value = "Vet successfully added!"
              }
              is Vet -> {
                try {
                  val updatedOffice =
                      officeRepository.getOffice(vetId).fold({ office ->
                        office.copy(vets = (office.vets + Firebase.auth.uid!!).distinct())
                      }) {
                        _vetClaimMessage.value = "Office does not exist"
                        throw IllegalStateException()
                      }
                  officeRepository.updateOffice(updatedOffice)
                  val updatedVet = user.copy(officeId = vetId)
                  userViewModel.updateUser(updatedVet)
                  _vetClaimMessage.value = "You successfully joined an office"
                } catch (e: Exception) {
                  _vetClaimMessage.value = "something went wrong somehow"
                }
              }
            }
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
