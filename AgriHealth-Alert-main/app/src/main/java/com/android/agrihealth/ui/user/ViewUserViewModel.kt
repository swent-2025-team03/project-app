package com.android.agrihealth.ui.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.UserRepositoryFirestore
import com.android.agrihealth.data.model.user.*
import kotlinx.coroutines.launch

sealed class ViewUserUiState {
  object Loading : ViewUserUiState()

  data class Success(val user: User) : ViewUserUiState()

  data class Error(val message: String) : ViewUserUiState()
}

open class ViewUserViewModel(
    private val targetUserId: String,
    private val userRepository: UserRepositoryFirestore = UserRepositoryFirestore()
) : ViewModel() {

  val uiState = mutableStateOf<ViewUserUiState>(ViewUserUiState.Loading)

  fun load(currentUser: User) {
    viewModelScope.launch {
      uiState.value = ViewUserUiState.Loading

      val target = userRepository.getUserFromId(targetUserId).getOrNull()

      if (target == null) {
        uiState.value = ViewUserUiState.Error("User does not exist.")
        return@launch
      }

      uiState.value = ViewUserUiState.Success(target)
    }
  }

  companion object {
    fun provideFactory(targetUserId: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewUserViewModel(
                targetUserId = targetUserId, userRepository = UserRepositoryFirestore())
                as T
          }
        }
  }
}
