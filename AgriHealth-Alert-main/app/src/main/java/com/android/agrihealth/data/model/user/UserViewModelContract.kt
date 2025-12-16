package com.android.agrihealth.data.model.user

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

/** ViewModel for managing user-related data and operations. */
interface UserViewModelContract {
  val uiState: StateFlow<UserUiState>

  /** Sets the current user. */
  fun setUser(user: User)

  /** Update user data (needed in profile screen) */
  fun updateUser(user: User): Deferred<Unit>

  /** Updating the officeId when creating or joining an office */
  fun updateVetOfficeId(officeId: String?): Deferred<Unit>
}
