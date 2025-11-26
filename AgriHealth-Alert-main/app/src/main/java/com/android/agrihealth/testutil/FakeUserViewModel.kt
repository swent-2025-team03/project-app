package com.android.agrihealth.testutil

import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.ui.user.UserViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserViewModel(initialUser: User) : UserViewModelContract {

  private val _user = MutableStateFlow(initialUser)
  override val user: StateFlow<User> = _user.asStateFlow()

  override fun setUser(user: User) {
    _user.value = user
  }

  override fun updateUser(user: User) {
    _user.value = user
  }
}
