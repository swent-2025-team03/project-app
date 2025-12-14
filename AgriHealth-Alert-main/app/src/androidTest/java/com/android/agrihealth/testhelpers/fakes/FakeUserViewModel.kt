package com.android.agrihealth.testhelpers.fakes

import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.UserUiState
import com.android.agrihealth.data.model.user.UserViewModelContract
import com.android.agrihealth.data.model.user.Vet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val defaultUser =
    Farmer(
        uid = "",
        firstname = "",
        lastname = "",
        email = "",
        address = null,
        linkedOffices = emptyList(),
        defaultOffice = "",
        isGoogleAccount = false,
        description = "",
        collected = false,
        deviceTokensFCM = emptySet())

class FakeUserViewModel(initialUser: User = defaultUser) : UserViewModelContract {
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
