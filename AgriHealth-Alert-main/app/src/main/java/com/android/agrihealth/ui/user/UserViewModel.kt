package com.android.agrihealth.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: connect to Firebase Auth

class UserViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel() {

  private val _userRole = MutableStateFlow<UserRole>(UserRole.FARMER)
  val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

  init {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      loadUserRole(currentUser.uid)
    }
  }

  private fun loadUserRole(userId: String) {
    viewModelScope.launch {
      viewModelScope.launch {
        val result = userRepository.getUserFromId(userId)

        result.fold(
            onSuccess = { user -> _userRole.value = user.role },
            onFailure = { e -> Log.e("UserViewModel", "Failed to load user role", e) })
      }
    }
  }

  fun refreshCurrentUser() {
    viewModelScope.launch {
      val currentUser = Firebase.auth.currentUser ?: return@launch
      loadUserRole(currentUser.uid)
    }
  }
}
