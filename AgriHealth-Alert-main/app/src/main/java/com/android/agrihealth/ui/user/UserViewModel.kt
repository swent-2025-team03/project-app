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

/**
 * ViewModel for managing user-related data and operations.
 *
 * @param userRepository The repository for user data operations.
 * @param auth The FirebaseAuth instance for authentication operations.
 */
class UserViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel() {

  private val _userRole = MutableStateFlow<UserRole>(UserRole.FARMER)

  /** The current user's role as a state flow. */
  val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

  init {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      loadUserRole(currentUser.uid)
    }
  }

  /**
   * Loads the user role for the given user ID and updates the state flow.
   *
   * @param userId The ID of the user whose role is to be loaded.
   */
  fun loadUserRole(userId: String) {
    viewModelScope.launch {
      val result = userRepository.getUserFromId(userId)

        result.fold(
            onSuccess = { user -> _userRole.value = user.role },
            onFailure = { e -> Log.e("UserViewModel", "Failed to load user role", e) })
      }
    }


  /**
   * Refreshes the current user's role by reloading it from the repository.
   */
  fun refreshCurrentUser() {
    viewModelScope.launch {
      val currentUser = auth.currentUser ?: return@launch
      loadUserRole(currentUser.uid)
    }
  }
}
