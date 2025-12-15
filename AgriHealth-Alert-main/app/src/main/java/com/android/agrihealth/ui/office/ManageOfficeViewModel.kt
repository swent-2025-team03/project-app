package com.android.agrihealth.ui.office

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.images.ImageUIState
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.loading.withLoadingState
import com.android.agrihealth.ui.user.UserViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ManageOfficeUiState(
    val office: Office? = null,
    val editableName: String = "",
    val editableDescription: String = "",
    val editableAddress: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val photoUri: Uri? = null,
    val uploadedImagePath: String? = null
)

class ManageOfficeViewModel(
    private val userViewModel: UserViewModelContract,
    private val officeRepository: OfficeRepository,
    private val imageViewModel: ImageViewModel
) : ViewModel() {

  private val _uiState = MutableStateFlow(ManageOfficeUiState())
  val uiState: StateFlow<ManageOfficeUiState> = _uiState

  fun loadOffice() {
    if (_uiState.value.office == null) {
      viewModelScope.launch {
        _uiState.withLoadingState(applyLoading = { s, loading -> s.copy(isLoading = loading) }) {
          val currentUser = userViewModel.uiState.value.user
          if (currentUser is Vet && currentUser.officeId != null) {
            officeRepository.getOffice(currentUser.officeId).fold({ office ->
              _uiState.value =
                  _uiState.value.copy(
                      office = office,
                      editableName = office.name,
                      editableDescription = office.description ?: "",
                      editableAddress = office.address?.toString() ?: "")
            }) { error ->
              _uiState.value =
                  _uiState.value.copy(
                      error =
                          "Couldn't load your office, make sure you are connected to the internet")
            }
          }
        }
      }
    }
  }

  fun onNameChange(value: String) {
    _uiState.value = _uiState.value.copy(editableName = value)
  }

  fun onDescriptionChange(value: String) {
    _uiState.value = _uiState.value.copy(editableDescription = value)
  }

  fun onAddressChange(value: String) {
    _uiState.value = _uiState.value.copy(editableAddress = value)
  }

  fun leaveOffice(onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) =
      viewModelScope.launch {
        val user = userViewModel.uiState.value.user
        val office = _uiState.value.office

        if (user !is Vet || office == null) return@launch

        try {
          // Remove officeId from vet first
          userViewModel.updateUser(user.copy(officeId = null, address = null))

          // Remove vet from office list
          val updatedOffice = office.copy(vets = office.vets.filterNot { it == user.uid })
          officeRepository.updateOffice(updatedOffice)

          _uiState.value = _uiState.value.copy(office = updatedOffice)
          onSuccess()
        } catch (e: Exception) {
          onError(e)
        }
      }

  suspend fun updateOffice(
      onSuccess: () -> Unit = {},
      onError: (Throwable) -> Unit = {},
      newAddress: Location? = null
  ) {
    try {
      val office = _uiState.value.office ?: return
      var uploadedPath = _uiState.value.uploadedImagePath

      _uiState.value.photoUri?.let { uri ->
        uploadedPath = imageViewModel.uploadAndWait(uri)
        _uiState.value = _uiState.value.copy(uploadedImagePath = uploadedPath, photoUri = null)
      }

      val updatedOffice =
          office.copy(
              name = _uiState.value.editableName,
              description = _uiState.value.editableDescription,
              address = newAddress,
              photoUrl = uploadedPath)
      officeRepository.updateOffice(updatedOffice)
      _uiState.value = _uiState.value.copy(office = updatedOffice)
      onSuccess()
    } catch (e: Throwable) {
      onError(e)
    }
  }



  fun setPhoto(uri: Uri?) {
    _uiState.value = _uiState.value.copy(photoUri = uri)
  }

  fun removePhoto() {
    _uiState.value = _uiState.value.copy(photoUri = null)
  }
}
