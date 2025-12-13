package com.android.agrihealth.data.model.user

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

interface UserViewModelContract {
  val user: StateFlow<User>

  fun setUser(user: User)

  fun updateUser(user: User): Deferred<Unit>

  fun updateVetOfficeId(officeId: String?): Deferred<Unit>
}
