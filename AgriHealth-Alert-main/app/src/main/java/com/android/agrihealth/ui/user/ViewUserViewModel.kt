package com.android.agrihealth.ui.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.user.UserRepository
import com.android.agrihealth.data.model.user.UserRepositoryFirestore
import com.android.agrihealth.data.model.office.OfficeRepository
import com.android.agrihealth.data.model.office.OfficeRepositoryFirestore
import com.android.agrihealth.data.model.user.*
import kotlinx.coroutines.launch

sealed class ViewUserUiState {
  object Loading : ViewUserUiState()

  data class Success(val user: User, val officeName: String? = null) : ViewUserUiState()

  data class Error(val message: String) : ViewUserUiState()
}

open class ViewUserViewModel(
    private val targetUserId: String,
    private val userRepository: UserRepository,
    private val officeRepository: OfficeRepository
) : ViewModel() {

  private val _uiState = mutableStateOf<ViewUserUiState>(ViewUserUiState.Loading)
  open val uiState: State<ViewUserUiState> = _uiState

  open fun load() {
    viewModelScope.launch {
      _uiState.value = ViewUserUiState.Loading

      val target = userRepository.getUserFromId(targetUserId).getOrNull()
      if (target == null) {
        _uiState.value = ViewUserUiState.Error("User does not exist.")
        return@launch
      }

      val officeName =
          if (target is Vet && target.officeId != null)
              officeRepository.getOffice(target.officeId).getOrNull()?.name ?: "None"
          else null

      _uiState.value = ViewUserUiState.Success(user = target, officeName = officeName)
    }
  }

  companion object {
    fun provideFactory(targetUserId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val userRepo = UserRepositoryFirestore()
            val officeRepo = OfficeRepositoryFirestore()
            @Suppress("UNCHECKED_CAST")
            return ViewUserViewModel(targetUserId, userRepo, officeRepo) as T
          }
        }
  }
}
