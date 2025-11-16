package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ManageOfficeUiState(
    val office: Office? = null,
    val editableName: String = "",
    val editableDescription: String = "",
    val editableAddress: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ManageOfficeViewModel(
    private val userViewModel: UserViewModel,
    private val officeRepository: OfficeRepository
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
                editableAddress = office?.address?.toString() ?: "")
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

  fun leaveOffice(onSuccess: () -> Unit) =
      viewModelScope.launch {
        val userId = userViewModel.user.value.uid
        val office = _uiState.value.office ?: return@launch

        // Remove officeId from vet
        userViewModel.updateVetOfficeId(null)

        // Remove vet from office list
        val updatedOffice = office.copy(vets = office.vets.filterNot { it == userId })
        officeRepository.updateOffice(updatedOffice)

        onSuccess()
      }

  fun updateOffice(onSuccess: () -> Unit = {}) =
      viewModelScope.launch {
        val office = _uiState.value.office ?: return@launch
        val updatedOffice =
            office.copy(
                name = _uiState.value.editableName,
                description = _uiState.value.editableDescription,
                // TODO: convert editableAddress back to Location when ready
            )
        officeRepository.updateOffice(updatedOffice)
        _uiState.value = _uiState.value.copy(office = updatedOffice)
        onSuccess()
      }
}
