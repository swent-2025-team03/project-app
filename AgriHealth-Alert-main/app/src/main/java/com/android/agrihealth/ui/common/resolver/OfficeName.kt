package com.android.agrihealth.ui.common.resolver

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
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
    deletedOffice: String = "Deleted office",
    noneOffice: String = "Not assigned to an office",
    vm: OfficeNameViewModel = viewModel(key = uid),
    onClick: (() -> Unit)? = null
) {
  val uiState by vm.uiState.collectAsState()
  LaunchedEffect(uid, deletedOffice, noneOffice) { vm.loadOffice(uid, deletedOffice, noneOffice) }
  Text(
      text = uiState,
      color = if (onClick != null) MaterialTheme.colorScheme.primary else LocalContentColor.current,
      textDecoration = if (onClick != null) TextDecoration.Underline else TextDecoration.None,
      modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier)
}

/**
 * String composable in case you need to display the Office Name in a context that requires a String
 */
@Composable
fun rememberOfficeName(officeId: String?): String {
  val vm: OfficeNameViewModel = viewModel(key = officeId)
  val name by vm.uiState.collectAsState()

  LaunchedEffect(officeId) {
    vm.loadOffice(
        uid = officeId, deletedOffice = "Deleted office", noneOffice = "Not assigned to an office")
  }

  return name
}
