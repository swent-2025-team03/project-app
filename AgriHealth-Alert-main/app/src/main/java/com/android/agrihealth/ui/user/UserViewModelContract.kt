package com.android.agrihealth.ui.user

import com.android.agrihealth.data.model.user.User
import kotlinx.coroutines.flow.StateFlow

interface UserViewModelContract {
  val user: StateFlow<User>

  fun setUser(user: User)

  fun updateUser(user: User)
}
