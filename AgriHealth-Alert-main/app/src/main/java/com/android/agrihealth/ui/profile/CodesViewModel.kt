package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import com.android.agrihealth.ui.user.UserViewModelContract
import java.lang.IllegalStateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CodesViewModel(
    private val userViewModel: UserViewModelContract = UserViewModel(),
    private val connectionRepository: ConnectionRepository,
    private val officeRepository: OfficeRepository = OfficeRepositoryProvider.get()
) : ViewModel() {

  private val _generatedCode = MutableStateFlow<String?>(null)
  private val _claimMessage = MutableStateFlow<String?>(null)
  private val _activeCodes = MutableStateFlow<List<String>>(emptyList())
  val generatedCode: StateFlow<String?> = _generatedCode
  val claimMessage: StateFlow<String?> = _claimMessage
  val activeCodes: StateFlow<List<String>> = _activeCodes

  fun generateCode(type: String) {
    val currentUser = userViewModel.user.value
    val vet = currentUser as? Vet ?: return

    viewModelScope.launch {
      val result = connectionRepository.generateCode(type)
      result.fold(
          onSuccess = { code ->
            _generatedCode.value = code
            val updatedVet =
                when (type) {
                  "FARMER" -> vet.copy(farmerConnectCodes = vet.farmerConnectCodes + code)
                  "VET" -> vet.copy(vetConnectCodes = vet.vetConnectCodes + code)
                  else -> vet
                }
            userViewModel.updateUser(updatedVet)
          },
          onFailure = { e -> _generatedCode.value = "Error: ${e.message}" })
    }
  }

  fun claimCode(code: String) {
    val user = userViewModel.user.value
    viewModelScope.launch {
      val result = connectionRepository.claimCode(code)
      result.fold(
          onSuccess = { officeId ->
            when (user) {
              is Farmer -> { // Update farmer: add officeId to linkedOffices (avoid duplicates)
                val updatedLinkedOffices = (user.linkedOffices + officeId).distinct()
                val newDefaultOffice = user.defaultOffice ?: officeId
                val updatedFarmer =
                    user.copy(
                        linkedOffices = updatedLinkedOffices, defaultOffice = newDefaultOffice)
                userViewModel.updateUser(updatedFarmer)

                _claimMessage.value = "Office successfully added!"
              }
              is Vet -> {
                try {
                  userViewModel.updateVetOfficeId(officeId).await()
                  val updatedOffice =
                      officeRepository.getOffice(officeId).fold({ office ->
                        office.copy(vets = (office.vets + user.uid))
                      }) {
                        _claimMessage.value = "Office does not exist"
                        throw IllegalStateException()
                      }
                  officeRepository.updateOffice(updatedOffice)
                  _claimMessage.value = "You successfully joined an office"
                } catch (_: Exception) {
                  if (_claimMessage.value == null)
                      _claimMessage.value = "Something went wrong somehow :("
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
                  else -> "Could not use code: ${e.message}"
                }
            _claimMessage.value = msg
          })
    }
  }

  fun loadActiveCodesForVet(vet: Vet) {
    viewModelScope.launch {
      val farmerCodes = connectionRepository.getValidCodes(vet, "FARMER")
      val vetCodes = connectionRepository.getValidCodes(vet, "VET")
      _activeCodes.value = farmerCodes + vetCodes
    }
  }
}
