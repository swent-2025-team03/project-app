package com.android.agrihealth.ui.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageOfficeViewModel(
    private val userViewModel: UserViewModel,
    private val officeRepository: OfficeRepository
) : ViewModel() {

  private val _office = MutableStateFlow<Office?>(null)
  val office: StateFlow<Office?> = _office

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading

  init {
    loadOffice()
  }

  private fun loadOffice() {
    viewModelScope.launch {
      val currentUser = userViewModel.user.value
      if (currentUser is com.android.agrihealth.data.model.user.Vet &&
          currentUser.officeId != null) {

        _office.value = officeRepository.getOffice(currentUser.officeId).getOrNull()
      }
      _isLoading.value = false
    }
  }

  fun leaveOffice() =
      viewModelScope.launch {
        val userId = userViewModel.user.value.uid
        val office = office.value ?: return@launch

        // 1. Remove officeId from vet
        userViewModel.updateVetOfficeId(null)

        // 2. Remove vet from office list
        val updatedOffice = office.copy(vets = office.vets.filterNot { it == userId })
        officeRepository.updateOffice(updatedOffice)
      }

  fun updateOffice(office: Office) =
      viewModelScope.launch {
        officeRepository.updateOffice(office)
        _office.value = office
      }
}
