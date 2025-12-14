package com.android.agrihealth.ui.user

import com.android.agrihealth.data.model.user.User
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

interface UserViewModelContract {
  val uiState: StateFlow<UserUiState>

  fun setUser(user: User)

  fun updateUser(user: User): Deferred<Unit>

  fun updateVetOfficeId(officeId: String?): Deferred<Unit>
}
