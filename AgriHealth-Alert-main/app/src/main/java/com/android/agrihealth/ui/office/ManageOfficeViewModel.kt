package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.UserViewModelContract
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.common.layout.withLoadingState
import com.android.agrihealth.ui.common.image.PhotoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ManageOfficeUiState(
    val office: Office? = null,
    val editableName: String = "",
    val editableDescription: String = "",
    val editableAddress: String = "",
    val isLoading: Boolean = false,
    val snackMessage: String? = null,
    val photoBytesToUpload: ByteArray? = null,
    val removeRemotePhoto: Boolean = false,
) {
  /**
   * Decides which photo to display depending on the state of the UI (i.e if a photo has been
   * removed, picked, ...)
   */
  val displayedPhoto: PhotoUi
    get() =
        when {
          photoBytesToUpload != null -> PhotoUi.Local(photoBytesToUpload)
          removeRemotePhoto -> PhotoUi.Empty
          office?.photoUrl != null -> PhotoUi.Remote(office.photoUrl)
          else -> PhotoUi.Empty
        }
}

class ManageOfficeViewModel(
    private val userViewModel: UserViewModelContract,
    private val officeRepository: OfficeRepository,
    private val imageViewModel: ImageViewModel
) : ViewModel() {

  private val _uiState = MutableStateFlow(ManageOfficeUiState())
  val uiState: StateFlow<ManageOfficeUiState> = _uiState

  init {
    loadOffice()
  }

  fun clearMessage() {
    _uiState.value = _uiState.value.copy(snackMessage = null)
  }

  fun loadOffice() {
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
                    snackMessage =
                        "Couldn't load your office, make sure you are connected to the internet")
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

      var newPhotoUrl: String? = if (_uiState.value.removeRemotePhoto) null else office.photoUrl

      _uiState.value.photoBytesToUpload?.let { bytes ->
        newPhotoUrl = imageViewModel.uploadAndWait(bytes)
        _uiState.value = _uiState.value.copy(photoBytesToUpload = null, removeRemotePhoto = false)
      }

      val updatedOffice =
          office.copy(
              name = _uiState.value.editableName,
              description = _uiState.value.editableDescription,
              address = newAddress,
              photoUrl = newPhotoUrl)
      officeRepository.updateOffice(updatedOffice)
      _uiState.value = _uiState.value.copy(office = updatedOffice)
      _uiState.value = _uiState.value.copy(snackMessage = "Changes successfully saved")
      onSuccess()
    } catch (e: Throwable) {
      onError(e)
    }
  }

  fun setPhoto(photoBytes: ByteArray?) {
    _uiState.value = _uiState.value.copy(photoBytesToUpload = photoBytes, removeRemotePhoto = false)
  }

  fun removePhoto() {
    _uiState.value = _uiState.value.copy(photoBytesToUpload = null, removeRemotePhoto = true)
  }
}

