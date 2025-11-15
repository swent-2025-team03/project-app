package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.ui.user.UserViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateOfficeViewModel(
    private val userViewModel: UserViewModel,
    private val officeRepository: OfficeRepository
) : ViewModel() {

  private val _name = MutableStateFlow("")
  val name = _name.asStateFlow()

  private val _description = MutableStateFlow("")
  val description = _description.asStateFlow()

  private val _address = MutableStateFlow("")
  val address = _address.asStateFlow()

  private val _loading = MutableStateFlow(false)
  val loading = _loading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error = _error.asStateFlow()

  fun onNameChange(value: String) {
    _name.value = value
  }

  fun onDescriptionChange(value: String) {
    _description.value = value
  }

  fun onAddressChange(value: String) {
    _address.value = value
  }

  fun createOffice(onSuccess: () -> Unit) {
    if (_name.value.isBlank()) {
      _error.value = "Office name cannot be empty."
      return
    }

    val vet = userViewModel.user.value

    viewModelScope.launch {
      _loading.value = true

      val officeId = UUID.randomUUID().toString()

      val createdOffice =
          Office(
              id = officeId,
              name = _name.value,
              description = _description.value.ifBlank { null },
              address = null, // TODO: implement Location selection
              ownerId = vet.uid,
              vets = listOf(vet.uid))

      try {
        officeRepository.addOffice(createdOffice)

        userViewModel.updateVetOfficeId(officeId)

        _loading.value = false
        onSuccess()
      } catch (e: Exception) {
        _error.value = e.message ?: "Failed to create office"
        _loading.value = false
      }
    }
  }
}
