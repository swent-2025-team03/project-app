package com.android.agrihealth.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.agrihealth.data.model.authentification.UserRepository
import com.android.agrihealth.data.model.authentification.UserRepositoryProvider
import com.android.agrihealth.data.model.user.Farmer
import com.android.agrihealth.data.model.user.User
import com.android.agrihealth.data.model.user.Vet
import com.android.agrihealth.ui.loading.withLoadingState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val defaultUser =
    Farmer(
        uid = "placeholder",
        firstname = "John",
        lastname = "Doe",
        email = "",
        address = null,
        linkedOffices = emptyList(),
        defaultOffice = null,
        isGoogleAccount = false)

data class UserUiState(val user: User = defaultUser, val isLoading: Boolean = false)

/**
 * ViewModel for managing user-related data and operations.
 *
 * @param userRepository The repository for user data operations.
 * @param auth The FirebaseAuth instance for authentication operations.
 */
open class UserViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val auth: FirebaseAuth = Firebase.auth,
    initialUser: User? = null
) : ViewModel(), UserViewModelContract {

  // private val _userRole = MutableStateFlow<UserRole>(UserRole.FARMER)
  private val _uiState = MutableStateFlow(UserUiState(user = initialUser ?: defaultUser))
  override val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

  // user id can be accessed using Firebase.auth.currentUser?.uid

  /** The current user's role as a state flow. */
  init {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      runBlocking { loadUser(currentUser.uid) }
    }
  }

  /**
   * Loads the user role for the given user ID and updates the state flow.
   *
   * @param userId The ID of the user whose role is to be loaded.
   */
  suspend fun loadUser(userId: String) {
    val result = userRepository.getUserFromId(userId)

    result.fold(
        onSuccess = { loadedUser -> _uiState.value = _uiState.value.copy(user = loadedUser) },
        onFailure = { e -> Log.e("UserViewModel", "Failed to load user role", e) })
  }

  /** Refreshes the current user's role by reloading it from the repository. */
  fun refreshCurrentUser() {
    viewModelScope.launch {
      val currentUser = auth.currentUser ?: return@launch
      loadUser(currentUser.uid)
    }
  }

  /** Update user data (needed in profile screen) */
  override fun updateUser(user: User): Deferred<Unit> {
    return viewModelScope.async {
      try {
        _uiState.withLoadingState(
            applyLoading = { state, loading -> state.copy(isLoading = loading) }) {
              userRepository.updateUser(user)
              _uiState.value = _uiState.value.copy(user = user) // update local state
        }
      } catch (e: Exception) {
        Log.e("UserViewModel", "Failed to update user", e)
      }
    }
  }

  /** Sets the current user. */
  override fun setUser(user: User) {
    _uiState.value = _uiState.value.copy(user = user)
  }

  /** Updating the officeId when creating or joining an office */
  override fun updateVetOfficeId(officeId: String?): Deferred<Unit> {
    return viewModelScope.async {
      val current = _uiState.value.user

      // Only vets should get an officeId
      if (current is Vet) {
        val updated = current.copy(officeId = officeId)

        try {
          _uiState.withLoadingState(
              applyLoading = { state, loading -> state.copy(isLoading = loading) }) {
                userRepository.updateUser(updated)
                _uiState.value = _uiState.value.copy(user = updated)
              }
        } catch (e: Exception) {
          Log.e("UserViewModel", "Failed to update vet officeId", e)
          throw e
        }
      }
    }
  }
}
