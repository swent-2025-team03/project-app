package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.user.UserUiState
import com.android.agrihealth.ui.user.UserViewModelContract
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserViewModel(initialUser: User) : UserViewModelContract {

  private val _uiState = MutableStateFlow(UserUiState(user = initialUser, isLoading = false))
  override val uiState: StateFlow<UserUiState> = _uiState

  override fun setUser(user: User) {
    _uiState.value = _uiState.value.copy(user = user)
  }

  override fun updateUser(user: User): Deferred<Unit> {
    _uiState.value = _uiState.value.copy(user = user)
    return CompletableDeferred(Unit)
  }

  override fun updateVetOfficeId(officeId: String?): Deferred<Unit> {
    val current = _uiState.value.user
    if (current is Vet) {
      _uiState.value = _uiState.value.copy(user = current.copy(officeId = officeId))
    }
    return CompletableDeferred(Unit)
  }
}
