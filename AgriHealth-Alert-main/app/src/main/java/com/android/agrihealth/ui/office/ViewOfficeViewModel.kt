package com.android.agrihealth.ui.office

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.office.Office
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class ViewOfficeUiState {
  object Loading : ViewOfficeUiState()

  data class Success(val office: Office) : ViewOfficeUiState()

  data class Error(val message: String) : ViewOfficeUiState()
}

open class ViewOfficeViewModel(
    private val targetOfficeId: String,
    private val officeRepository: OfficeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

  private val _uiState = mutableStateOf<ViewOfficeUiState>(ViewOfficeUiState.Loading)
  open val uiState: State<ViewOfficeUiState> = _uiState

  open fun load() {
    viewModelScope.launch(dispatcher) {
      _uiState.value = ViewOfficeUiState.Loading

      val officeResult = officeRepository.getOffice(targetOfficeId)
      val office = officeResult.getOrNull()
      if (office == null) {
        _uiState.value = ViewOfficeUiState.Error("Office does not exist.")
        return@launch
      }

      _uiState.value = ViewOfficeUiState.Success(office)
    }
  }

  companion object {
    fun provideFactory(targetOfficeId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val officeRepo = OfficeRepositoryFirestore()
            @Suppress("UNCHECKED_CAST")
            return ViewOfficeViewModel(targetOfficeId, officeRepo, Dispatchers.Main) as T
          }
        }
  }
}
