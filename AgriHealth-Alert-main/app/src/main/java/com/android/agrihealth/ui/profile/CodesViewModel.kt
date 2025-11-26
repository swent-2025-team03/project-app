package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import java.lang.IllegalStateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CodesViewModel(
    private val userViewModel: UserViewModel,
    private val connectionRepository: ConnectionRepository,
    private val officeRepository: OfficeRepository = OfficeRepositoryProvider.get()
) : ViewModel() {

  private val _generatedCode = MutableStateFlow<String?>(null)
  private val _claimMessage = MutableStateFlow<String?>(null)
  val claimMessage: StateFlow<String?> = _claimMessage

  val generatedCode: StateFlow<String?> = _generatedCode

  fun generateCode() {
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
                  "Something went wrong somehow :("
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
}
