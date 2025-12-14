package com.android.agrihealth.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.connection.ConnectionRepository
import com.android.agrihealth.data.model.device.notifications.Notification
import com.android.agrihealth.data.model.device.notifications.NotificationHandlerFirebase
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
  private val _farmerCodes = MutableStateFlow<List<String>>(emptyList())
  val farmerCodes: StateFlow<List<String>> = _farmerCodes
  private val _vetCodes = MutableStateFlow<List<String>>(emptyList())
  val vetCodes: StateFlow<List<String>> = _vetCodes
  val generatedCode: StateFlow<String?> = _generatedCode
  val claimMessage: StateFlow<String?> = _claimMessage

  fun resetClaimMessage() {
    _claimMessage.value = null
  }

  fun generateCode() {
    val currentUser = userViewModel.user.value
    val vet = currentUser as? Vet ?: return

    viewModelScope.launch {
      val result = connectionRepository.generateCode()
      result.fold(
          onSuccess = { code ->
            _generatedCode.value = code
            val updatedVet =
                when (connectionRepository.type) {
                  "farmerToOffice" -> vet.copy(farmerConnectCodes = vet.farmerConnectCodes + code)
                  "vetToOffice" -> vet.copy(vetConnectCodes = vet.vetConnectCodes + code)
                  else -> vet
                }
            userViewModel.updateUser(updatedVet)
          },
          onFailure = { _ ->
            _generatedCode.value =
                "Something went wrong, please make sure you are connected to the internet."
          })
    }
  }

  fun claimCode(code: String) {
    val user = userViewModel.user.value
    val userName =
        listOfNotNull(user.firstname, user.lastname)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { user.uid }
    viewModelScope.launch {
      val result = connectionRepository.claimCode(code)
      result.fold(
          onSuccess = { officeId ->
            val destinationUids = officeRepository.getVetsInOffice(officeId)
            when (user) {
              is Farmer -> { // Update farmer: add officeId to linkedOffices (avoid duplicates)
                val updatedLinkedOffices = (user.linkedOffices + officeId).distinct()
                val newDefaultOffice = user.defaultOffice ?: officeId
                val updatedFarmer =
                    user.copy(
                        linkedOffices = updatedLinkedOffices, defaultOffice = newDefaultOffice)
                userViewModel.updateUser(updatedFarmer)

                // Send a notification
                val description = "A new farmer: '${userName}' just got connected to your office!"
                destinationUids.forEach { uid ->
                  val notification =
                      Notification.ConnectOffice(destinationUid = uid, description = description)
                  val messagingService = NotificationHandlerFirebase()
                  messagingService.uploadNotification(notification)
                }

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

                  // Send a notification
                  val description = "A new vet: '${userName}' just joined your office!"
                  destinationUids.forEach { uid ->
                    val notification =
                        Notification.JoinOffice(destinationUid = uid, description = description)
                    val messagingService = NotificationHandlerFirebase()
                    messagingService.uploadNotification(notification)
                  }

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
      _farmerCodes.value = connectionRepository.getValidCodes(vet, CodeType.FARMER)
      _vetCodes.value = connectionRepository.getValidCodes(vet, CodeType.VET)
    }
  }
}
