package com.android.agrihealth.ui.office

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.images.ImageUIState
import com.android.agrihealth.data.model.images.ImageViewModel
import com.android.agrihealth.data.model.location.Location
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.report.AddReportFeedbackTexts
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
    private val imageViewModel: ImageViewModel = ImageViewModel()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ManageOfficeUiState())
  val uiState: StateFlow<ManageOfficeUiState> = _uiState

  init {
    loadOffice()
  }

  fun loadOffice() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)

      val currentUser = userViewModel.user.value
      if (currentUser is Vet && currentUser.officeId != null) {
        val office = officeRepository.getOffice(currentUser.officeId).getOrNull()
        _uiState.value =
            _uiState.value.copy(
                office = office,
                editableName = office?.name ?: "",
                editableDescription = office?.description ?: "",
                editableAddress = office?.address?.toString() ?: "",
                photoUri = null)
      }

      _uiState.value = _uiState.value.copy(isLoading = false)
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
        val user = userViewModel.user.value
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

  fun updateOffice(
      onSuccess: () -> Unit = {},
      onError: (Throwable) -> Unit = {},
      newAddress: Location? = null
  ) =
      viewModelScope.launch {
        var uploadedPath = _uiState.value.uploadedImagePath
        _uiState.value.photoUri?.let { uri ->
          imageViewModel.upload(uri)
          val resultState =
              imageViewModel.uiState.first {
                it is ImageUIState.UploadSuccess || it is ImageUIState.Error
              }
          when (resultState) {
            is ImageUIState.UploadSuccess -> {
              uploadedPath = resultState.path
              Log.d("ManageOfficeViewModel", "uploadedPath = $uploadedPath")
              _uiState.value = _uiState.value.copy(uploadedImagePath = resultState.path)

              val office = _uiState.value.office ?: return@launch
              val updatedOffice =
                  office.copy(
                      name = _uiState.value.editableName,
                      description = _uiState.value.editableDescription,
                      address = newAddress,
                      photoUrl = uploadedPath)
              officeRepository.updateOffice(updatedOffice)
              _uiState.value = _uiState.value.copy(office = updatedOffice)
              onSuccess()
            }
            is ImageUIState.Error -> {
              onError(resultState.e)
              return@launch
            }
            else -> {
              onError(IllegalStateException(AddReportFeedbackTexts.UNKNOWN))
              return@launch
            }
          }
        }
      }

  fun setPhoto(uri: Uri?) {
    _uiState.value = _uiState.value.copy(photoUri = uri)
  }

  fun removePhoto() {
    _uiState.value = _uiState.value.copy(photoUri = null)
  }

  fun setUploadedPhotoUrl(url: String?) {
    _uiState.value = _uiState.value.copy(uploadedImagePath = url)
  }
}
