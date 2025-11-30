package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateOfficeUiState(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class CreateOfficeViewModel(
    private val userViewModel: UserViewModel,
    private val officeRepository: OfficeRepository = OfficeRepositoryProvider.get()
) : ViewModel() {

  private val user = userViewModel.user.value
  private val _uiState = MutableStateFlow(CreateOfficeUiState(address = user.address?.name ?: ""))
  val uiState = _uiState.asStateFlow()

  fun onNameChange(value: String) {
    _uiState.update { it.copy(name = value) }
  }

  fun onDescriptionChange(value: String) {
    _uiState.update { it.copy(description = value) }
  }

  fun createOffice(onSuccess: () -> Unit) {
    val state = _uiState.value

    if (state.name.isBlank()) {
      _uiState.update { it.copy(error = "Office name cannot be empty.") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(loading = true, error = null) }

      val officeId = officeRepository.getNewUid()

      val createdOffice =
          Office(
              id = officeId,
              name = state.name,
              description = state.description.ifBlank { null },
              address = user.address,
              ownerId = user.uid,
              vets = listOf(user.uid))

      try {
        officeRepository.addOffice(createdOffice)

        try {
          // Attempt to update user data
          userViewModel.updateVetOfficeId(officeId)
        } catch (e: Exception) {
          // Rollback if user update fails
          officeRepository.deleteOffice(officeId)
          throw e
        }

        _uiState.update { it.copy(loading = false) }
        onSuccess()
      } catch (e: Exception) {

        _uiState.update { it.copy(loading = false, error = e.message ?: "Failed to create office") }
      }
    }
  }
}
