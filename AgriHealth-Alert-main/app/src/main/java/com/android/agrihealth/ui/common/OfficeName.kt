package com.android.agrihealth.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficeNameViewModel(
    private val repository: OfficeRepository = OfficeRepositoryProvider.get()
) : ViewModel() {
  private val _uiState = MutableStateFlow("...")
  val uiState: StateFlow<String> = _uiState

  fun loadOffice(uid: String?, deletedOffice: String, noneOffice: String) {
    if (uid == null) {
      _uiState.value = noneOffice
    } else {
      viewModelScope.launch {
        val office = repository.getOffice(uid).fold({ office -> office.name }) { deletedOffice }
        _uiState.value = office
      }
    }
  }
}

@Composable
fun OfficeName(
    uid: String?,
    modifier: Modifier = Modifier,
    deletedOffice: String = "deleted Office",
    noneOffice: String = "not assigned to an Office",
    vm: OfficeNameViewModel = viewModel(key = uid)
) {
  val uiState by vm.uiState.collectAsState()
  LaunchedEffect(uid, deletedOffice, noneOffice) { vm.loadOffice(uid, deletedOffice, noneOffice) }
  Text(uiState, modifier)
}
